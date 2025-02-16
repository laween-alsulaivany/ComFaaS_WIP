package comfaas;

public class EdgeMetrics {
    private String ip;
    private double cpuAvailability;
    private double freeMemoryMB;
    private double latency;

    public EdgeMetrics(String ip, double cpuAvailability, double freeMemoryMB, double latency) {
        this.ip = ip;
        this.cpuAvailability = cpuAvailability;
        this.freeMemoryMB = freeMemoryMB;
        this.latency = latency;
    }

    public String getIp() {
        return ip;
    }

    public double getCpuAvailability() {
        return cpuAvailability;
    }

    public double getFreeMemoryMB() {
        return freeMemoryMB;
    }

    public double getLatency() {
        return latency;
    }

    public void setCpuAvailability(double cpuAvailability) {
        this.cpuAvailability = cpuAvailability;
    }

    public void setFreeMemoryMB(double freeMemoryMB) {
        this.freeMemoryMB = freeMemoryMB;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    @Override
    public String toString() {
        return String.format("EdgeMetrics[ip=%s, cpu=%.2f, memory=%.2fMB, latency=%.2fms]",
                ip, cpuAvailability, freeMemoryMB, latency);
    }
}
