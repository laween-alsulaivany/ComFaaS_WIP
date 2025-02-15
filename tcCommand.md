The `tc` command is part of the iproute2 package and is used to configure the Linux kernel’s traffic control mechanisms. It allows you to shape, schedule, and manage network traffic—for example, to simulate network delays, limit bandwidth, or introduce packet loss. Here’s a general overview of how to use it on Ubuntu:

---

### 1. **Basic Concepts**

- **qdisc (Queueing Discipline):** The fundamental building block for traffic control. qdiscs can be attached to network devices to manage how packets are enqueued and dequeued.
- **Classes:** Used in hierarchical qdiscs (like HTB) to subdivide bandwidth among different flows.
- **Filters:** Direct packets into specific classes based on criteria like source/destination IP, port, etc.

---

### 2. **Common Tasks and Examples**

#### **a. Adding a Simple Delay**

To simulate a network delay (using the `netem` qdisc), you can add a delay of 100 milliseconds on an interface (e.g., `eth0`):

```bash
sudo tc qdisc add dev eth0 root netem delay 100ms
```

This command attaches a root qdisc with a 100ms delay to `eth0`.

#### **b. Changing an Existing qdisc**

If you already have a qdisc set up and want to change its parameters (for instance, increasing the delay):

```bash
sudo tc qdisc change dev eth0 root netem delay 200ms
```

#### **c. Removing a qdisc**

To remove all traffic control settings from an interface (restoring it to normal):

```bash
sudo tc qdisc del dev eth0 root
```

---

### 3. **Using Hierarchical Token Bucket (HTB) for Bandwidth Control**

For more granular control, you might use the HTB qdisc to allocate bandwidth. For example, to limit an interface to 1 Mbit/s and create two classes:

1. **Attach the HTB qdisc to the interface:**

   ```bash
   sudo tc qdisc add dev eth0 root handle 1: htb default 12
   ```

2. **Add a parent class with a maximum rate:**

   ```bash
   sudo tc class add dev eth0 parent 1: classid 1:1 htb rate 1mbit
   ```

3. **Create child classes:**

   ```bash
   sudo tc class add dev eth0 parent 1:1 classid 1:10 htb rate 600kbit ceil 1mbit
   sudo tc class add dev eth0 parent 1:1 classid 1:12 htb rate 400kbit ceil 1mbit
   ```

4. **Add filters to direct traffic into these classes:**  
   For instance, using IP filtering to direct traffic to class `1:10`:

   ```bash
   sudo tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip dst 192.168.1.100/32 flowid 1:10
   ```

This setup divides your bandwidth, ensuring that traffic matching the filter gets up to 600kbit while other traffic gets up to 400kbit.

---

### 4. **Additional Tips**

- **Listing current configurations:**  
  To see the current qdiscs and classes on an interface, use:

  ```bash
  sudo tc qdisc show dev eth0
  sudo tc class show dev eth0
  ```

To simulate a 1% packet loss using the tc command with the netem qdisc, you can use the following command:

```bash
sudo tc qdisc add dev eth0 root netem loss 1%
```

### Explanation

- **`tc qdisc add dev eth0 root netem loss 1%`**  
  This command attaches a netem qdisc to the `eth0` network interface and configures it to drop 1% of the packets, simulating a 1% packet loss.

### Removing the Configuration

If you need to remove this configuration later and restore normal traffic, run:

```bash
sudo tc qdisc del dev eth0 root
```

Make sure to replace `eth0` with the appropriate network interface if your system uses a different name.


Yes, you can apply a delay to traffic for specific IP addresses by using tc filters to direct matching traffic into a qdisc that applies the delay. One common approach is to set up a hierarchical qdisc (such as HTB) with separate classes and then attach a netem qdisc (which can add delay, loss, etc.) to the class for the targeted IP.

Below is an example that applies a 200 ms delay to all traffic destined for the IP address 192.168.1.100 on interface eth0:

```bash
#!/bin/bash
# Remove any existing qdisc on eth0.
sudo tc qdisc del dev eth0 root 2>/dev/null

# Add a root HTB qdisc.
sudo tc qdisc add dev eth0 root handle 1: htb default 20

# Create a parent class that covers all traffic.
sudo tc class add dev eth0 parent 1: classid 1:1 htb rate 1000mbit

# Create two child classes:
# Class 1:10 for traffic to 192.168.1.100 (with delay).
# Class 1:20 for all other traffic.
sudo tc class add dev eth0 parent 1:1 classid 1:10 htb rate 1000mbit
sudo tc class add dev eth0 parent 1:1 classid 1:20 htb rate 1000mbit

# Attach a netem qdisc to class 1:10 that adds a 200ms delay.
sudo tc qdisc add dev eth0 parent 1:10 handle 10: netem delay 200ms

# Add a filter to direct traffic destined for 192.168.1.100 to class 1:10.
sudo tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip dst 192.168.1.100/32 flowid 1:10
```

### Explanation

- **Step 1:**  
  We clear any existing qdisc on `eth0` to avoid conflicts.

- **Step 2:**  
  A root HTB qdisc is added with a handle `1:`. The default class is set to `20` (for non-matching traffic).

- **Step 3:**  
  A parent class `1:1` is created with a high bandwidth (here, 1000mbit) so that bandwidth isn’t a limiting factor.

- **Step 4:**  
  Two child classes are added under the parent:
  - **1:10:** For traffic that matches our filter (destined to 192.168.1.100).
  - **1:20:** For all other traffic (using the default).
  
- **Step 5:**  
  A netem qdisc is attached to class `1:10` to add a 200 ms delay to any packets routed to this class.

- **Step 6:**  
  A tc filter using the u32 classifier matches IP packets destined for 192.168.1.100 (with a /32 mask for a single IP) and directs them to flowid `1:10`.

This setup means that any traffic leaving interface `eth0` and going to 192.168.1.100 will experience a 200 ms delay, while other traffic is unaffected. You can adjust the IP, delay, and interface as needed for your use case.