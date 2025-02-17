# memory_stress.py
def memory_stress():
    big_list = []
    try:
        while True:
            # Allocate 1MB chunk of memory per iteration
            big_list.append(bytearray(1024 * 1024))
            print(f"Allocated {len(big_list)} MB")
    except MemoryError:
        print("Memory exhausted!")

if __name__ == "__main__":
    memory_stress()
