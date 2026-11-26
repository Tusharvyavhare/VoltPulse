package com.voltpulse.coa;
//co1
public class CpuSimulator {
    private long accumulator = 0;
    private long regR1 = 0; // Energy Consumed (kWh)
    private long regR2 = 0; // Tariff Rate (₹)
    private int programCounter = 0;

    public void loadData(long energy, long rate) {
        this.regR1 = energy;
        this.regR2 = rate;
        programCounter++;
    }

    // ALU Arithmetic Execution
    public void executeALUOperation(String operation) {
        if (operation.equalsIgnoreCase("MUL")) {
            this.accumulator = this.regR1 * this.regR2;
        } else if (operation.equalsIgnoreCase("ADD")) {
            this.accumulator = this.regR1 + this.regR2;
        }
        programCounter++;
    }

    public long getAccumulatorResult() {
        return this.accumulator;
    }
}