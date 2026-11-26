package com.voltpulse.coa;

public class CoaIntegrationManager {
    //co 4
    public static void runCompleteCoaPipeline() {
        System.out.println("=== VoltPulse COA Integration Pipeline Started ===");
        
        // 1. CO1: CPU Simulator Execution
        CpuSimulator cpu = new CpuSimulator();
        cpu.loadData(30, 15); // 30 kWh @ ₹15
        cpu.executeALUOperation("MUL");
        System.out.println("CO1 - ALU Calculated Bill via Registers: ₹" + cpu.getAccumulatorResult());
        
        // 2. CO2: 64-bit Assembly Conditional Branching Check
        AssemblySimulationDemo asmDemo = new AssemblySimulationDemo();
        asmDemo.simulate64BitBranching(30, 15);
        
        // 3. CO3: Intensive Successive Addition & String Processing
        IntensiveProcessingModule intensive = new IntensiveProcessingModule();
        long intensiveResult = intensive.computeSuccessiveAddition(30, 15);
        System.out.println(" Result: ₹" + intensiveResult);
        intensive.processTransactionString("VP-TXN-99882");
        
        
    }
}