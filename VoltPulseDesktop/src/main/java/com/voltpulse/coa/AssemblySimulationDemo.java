package com.voltpulse.coa;

/* 
 * CO2: 64-bit Assembly Equivalent Logic for VoltPulse Billing & Conditional Branching
 * Corresponding x86-64 Assembly representation:
 */
public class AssemblySimulationDemo {
    
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