package com.voltpulse.coa;

/* 
 * CO2: 64-bit Assembly Equivalent Logic for VoltPulse Billing & Conditional Branching
 * Corresponding x86-64 Assembly representation:
 */
public class AssemblySimulationDemo {
    /*
     * 64-bit Assembly Code Snippet (x86_64):
     * ----------------------------------------
     * global process_billing_64
     * process_billing_64:
     *     mov rax, rdi        ; Move Energy (64-bit rdi) to RAX
     *     imul rax, rsi       ; Multiply by Tariff Rate (rsi) -> ALU
     *     cmp rax, 5000       ; Compare total bill with threshold (5000 INR)
     *     jle valid_limit     ; Conditional Branching if <= 5000
     *     mov rdx, 1          ; Flag high-consumption alert
     *     ret
     * valid_limit:
     *     xor rdx, rdx        ; Clear alert flag
     *     ret
     */
    
    public boolean simulate64BitBranching(long energy, long tariffRate) {
        long rax = energy * tariffRate; // 64-bit multiplication
        long threshold = 5000L;
        
        // Conditional Branching Simulation
        if (rax <= threshold) {
            System.out.println("[64-bit Assembly CO2] Bill within normal limit: ₹" + rax);
            return false; // No excess flag
        } else {
            System.out.println("[64-bit Assembly CO2] High consumption alert triggered! Bill: ₹" + rax);
            return true;  // Excess flag set (rdx = 1)
        }
    }
} 