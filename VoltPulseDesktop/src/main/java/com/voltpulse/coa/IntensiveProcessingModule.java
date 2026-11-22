package com.voltpulse.coa;

public class IntensiveProcessingModule {

    /* 
     * CO3: Computationally Intensive Operation using Successive Addition
     * (Simulating hardware multiplication via repeated additions)
     */
    public long computeSuccessiveAddition(long energyUnits, long costPerUnit) {
        long accumulator = 0;
        long counter = energyUnits;
        
        // Successive Addition Algorithm (Loops mimicking low-level assembly repetitive ADD)
        while (counter > 0) {
            accumulator += costPerUnit;
            counter--;
        }
        return accumulator; // Equivalent to multiplication result
    }

    /*
     * CO3: 64-bit String / Data Processing Requirement
     * Processing transaction log strings or EV station IDs at byte/word level
     */
    public void processTransactionString(String transactionId) {
        byte[] bytes = transactionId.getBytes();
        long checksum = 0;
        
        // Low-level byte processing / word-level manipulation simulation
        for (byte b : bytes) {
            checksum ^= b; // XOR bitwise operation simulating 64-bit data register processing
        }
        System.out.println("[64-bit String Processing CO3] Processed ID: " + transactionId + " | Checksum: " + checksum);
    }
}