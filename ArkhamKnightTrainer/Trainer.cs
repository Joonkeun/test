using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Threading;

namespace ArkhamKnightTrainer
{
    class Trainer
    {
        // Windows API
        [DllImport("kernel32.dll")]
        static extern IntPtr OpenProcess(int dwDesiredAccess, bool bInheritHandle, int dwProcessId);

        [DllImport("kernel32.dll")]
        static extern bool ReadProcessMemory(IntPtr hProcess, IntPtr lpBaseAddress, byte[] lpBuffer, int dwSize, out int lpNumberOfBytesRead);

        [DllImport("kernel32.dll")]
        static extern bool WriteProcessMemory(IntPtr hProcess, IntPtr lpBaseAddress, byte[] lpBuffer, int dwSize, out int lpNumberOfBytesWritten);

        [DllImport("kernel32.dll")]
        static extern bool CloseHandle(IntPtr hObject);

        [DllImport("kernel32.dll")]
        static extern IntPtr VirtualAllocEx(IntPtr hProcess, IntPtr lpAddress, uint dwSize, uint flAllocationType, uint flProtect);

        const int PROCESS_ALL_ACCESS = 0x1F0FFF;

        static IntPtr processHandle = IntPtr.Zero;
        static bool techPointsEnabled = false;
        static Thread cheatThread;

        // ===================================================================
        // 아래 주소는 예시입니다. Cheat Engine으로 실제 주소를 찾아서 교체하세요.
        // 방법: Cheat Engine 실행 → 게임 프로세스 연결 → 테크포인트 값 스캔
        // ===================================================================
        static long techPointsOffset = 0x00000000; // 실제 오프셋으로 교체 필요
        static long baseAddress     = 0x00000000; // 실제 베이스 주소로 교체 필요

        // 포인터 체인 (멀티레벨 포인터인 경우 사용)
        static long[] pointerChain = new long[] { 0x0, 0x0, 0x0 }; // 실제 값으로 교체

        static void Main(string[] args)
        {
            Console.Title = "Batman: Arkham Knight Trainer";
            PrintBanner();

            while (true)
            {
                if (!AttachToGame())
                {
                    Console.ForegroundColor = ConsoleColor.Yellow;
                    Console.WriteLine("[!] BatmanAK.exe 프로세스를 찾는 중... 게임을 먼저 실행하세요.");
                    Console.ResetColor();
                    Thread.Sleep(3000);
                    continue;
                }

                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine("[+] 게임에 연결 성공!");
                Console.ResetColor();
                PrintMenu();
                RunInputLoop();
                break;
            }
        }

        static void PrintBanner()
        {
            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.WriteLine("========================================");
            Console.WriteLine("  Batman: Arkham Knight Trainer v1.0");
            Console.WriteLine("  Tech Points Hack");
            Console.WriteLine("========================================");
            Console.ResetColor();
        }

        static void PrintMenu()
        {
            Console.WriteLine();
            Console.ForegroundColor = ConsoleColor.White;
            Console.WriteLine("[1] 테크포인트 무한 ON/OFF (F1)");
            Console.WriteLine("[2] 테크포인트 즉시 최대값 설정");
            Console.WriteLine("[3] 현재 테크포인트 값 읽기");
            Console.WriteLine("[0] 종료");
            Console.ResetColor();
            Console.WriteLine();
        }

        static bool AttachToGame()
        {
            Process[] processes = Process.GetProcessesByName("BatmanAK");
            if (processes.Length == 0) return false;

            processHandle = OpenProcess(PROCESS_ALL_ACCESS, false, processes[0].Id);
            return processHandle != IntPtr.Zero;
        }

        static void RunInputLoop()
        {
            // 핫키 감지 스레드 시작
            cheatThread = new Thread(HotkeyThread);
            cheatThread.IsBackground = true;
            cheatThread.Start();

            while (true)
            {
                Console.Write("> ");
                string input = Console.ReadLine();

                switch (input?.Trim())
                {
                    case "1":
                        ToggleTechPoints();
                        break;
                    case "2":
                        SetTechPointsMax();
                        break;
                    case "3":
                        ReadTechPoints();
                        break;
                    case "0":
                        CleanUp();
                        return;
                    default:
                        Console.WriteLine("[?] 올바른 메뉴를 선택하세요.");
                        break;
                }
            }
        }

        static void HotkeyThread()
        {
            while (true)
            {
                if (GetAsyncKeyState(0x70) != 0) // F1
                {
                    ToggleTechPoints();
                    Thread.Sleep(500); // 디바운스
                }
                Thread.Sleep(50);
            }
        }

        [DllImport("user32.dll")]
        static extern short GetAsyncKeyState(int vKey);

        static void ToggleTechPoints()
        {
            techPointsEnabled = !techPointsEnabled;
            Console.ForegroundColor = techPointsEnabled ? ConsoleColor.Green : ConsoleColor.Red;
            Console.WriteLine($"[*] 테크포인트 무한: {(techPointsEnabled ? "ON" : "OFF")}");
            Console.ResetColor();

            if (techPointsEnabled)
            {
                Thread t = new Thread(() =>
                {
                    while (techPointsEnabled)
                    {
                        WriteTechPoints(9999); // 최대값 유지
                        Thread.Sleep(100);
                    }
                });
                t.IsBackground = true;
                t.Start();
            }
        }

        static void SetTechPointsMax()
        {
            bool success = WriteTechPoints(9999);
            if (success)
            {
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine("[+] 테크포인트를 9999로 설정했습니다.");
            }
            else
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("[-] 쓰기 실패. 주소를 확인하세요.");
            }
            Console.ResetColor();
        }

        static void ReadTechPoints()
        {
            IntPtr addr = ResolvePointer();
            if (addr == IntPtr.Zero)
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("[-] 주소 해석 실패. 포인터 체인을 확인하세요.");
                Console.ResetColor();
                return;
            }

            byte[] buffer = new byte[4];
            int bytesRead;
            if (ReadProcessMemory(processHandle, addr, buffer, 4, out bytesRead))
            {
                int value = BitConverter.ToInt32(buffer, 0);
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine($"[*] 현재 테크포인트: {value}");
                Console.ResetColor();
            }
            else
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("[-] 읽기 실패.");
                Console.ResetColor();
            }
        }

        static bool WriteTechPoints(int value)
        {
            IntPtr addr = ResolvePointer();
            if (addr == IntPtr.Zero) return false;

            byte[] buffer = BitConverter.GetBytes(value);
            int bytesWritten;
            return WriteProcessMemory(processHandle, addr, buffer, buffer.Length, out bytesWritten);
        }

        // 멀티레벨 포인터 해석
        // 단순 정적 주소 사용 시: baseAddress + techPointsOffset 만 반환하면 됩니다.
        static IntPtr ResolvePointer()
        {
            if (processHandle == IntPtr.Zero) return IntPtr.Zero;

            // 정적 주소 방식 (포인터 체인이 없는 경우)
            if (pointerChain.Length == 0 || (pointerChain.Length == 1 && pointerChain[0] == 0))
            {
                return new IntPtr(baseAddress + techPointsOffset);
            }

            // 멀티레벨 포인터 방식
            byte[] buffer = new byte[8];
            int bytesRead;
            IntPtr current = new IntPtr(baseAddress);

            foreach (long offset in pointerChain)
            {
                if (!ReadProcessMemory(processHandle, current, buffer, 8, out bytesRead))
                    return IntPtr.Zero;

                current = new IntPtr(BitConverter.ToInt64(buffer, 0) + offset);
            }

            return new IntPtr(current.ToInt64() + techPointsOffset);
        }

        static void CleanUp()
        {
            techPointsEnabled = false;
            if (processHandle != IntPtr.Zero)
                CloseHandle(processHandle);
            Console.WriteLine("[*] 트레이너 종료.");
        }
    }
}
