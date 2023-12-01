import ctypes as CT
import atexit

class MPIpy:
    c_code = CT.CDLL("./comp_MPIpy.so")
    comm_func = c_code.communicator
    comm_func.restype = CT.c_int
    cworld = comm_func()
    MASTER = 0

    minidict = {
        "MAX": 1,
        "MIN":2,
        "SUM":3,
        "PROD":4,
        "LAND":5, #!! be carefull with these.
        "LOR":6,  #!! python datatype of int might mess things up.
        "BAND":7, #!!
        "BOR":8   #!!
    }

    def __init__(self):
        """
        This is set up so that the user does not have to initialize,
        finalize, and acknoledge comm (at bottom of init).
        Need to go over reduce, bcast, scatter... reduce the code.
        """

        # all function preperation.
        self.__rank = MPIpy.c_code.mpi_comm_rank
        self.__rank.argtypes = [CT.c_int]
        self.__rank.restype = CT.c_int

        self.__size = MPIpy.c_code.mpi_comm_size
        self.__size.argtypes = [CT.c_int]
        self.__size.restype = CT.c_int

        self.__int_send = MPIpy.c_code.mpi_send_int
        self.__int_send.argtypes = [CT.c_int, CT.c_int, CT.c_int, CT.c_int, CT.c_int]

        self.__int_recv = MPIpy.c_code.mpi_recv_int
        self.__int_recv.argtypes = [CT.c_int, CT.c_int, CT.c_int, CT.c_int]
        self.__int_recv.restype = CT.c_int

        self.__array_send_int = MPIpy.c_code.mpi_send_int_array
        self.__array_send_int.argtypes = [CT.c_void_p, CT.c_int, CT.c_int, CT.c_int, CT.c_int]
        self.__array_send_int.restype = CT.c_int

        self.__array_send_double = MPIpy.c_code.mpi_send_double_array
        self.__array_send_double.argtypes = [CT.c_void_p, CT.c_int, CT.c_int, CT.c_int, CT.c_int]

        self.__array_recv_int = MPIpy.c_code.mpi_recv_int_array
        self.__array_recv_int.argtypes = [CT.c_void_p, CT.c_int, CT.c_int, CT.c_int]
        self.__array_recv_int.restype = CT.c_int

        self.temp_P = CT.c_void_p()

        self.__array_recv_double = MPIpy.c_code.mpi_recv_double_array
        self.__array_recv_double.argtypes = [CT.c_void_p, CT.c_int, CT.c_int, CT.c_int]
        self.__array_recv_double.restype = CT.c_int

        self.__reduce_Sum_int = MPIpy.c_code.reduceSum
        self.__reduce_Sum_int.argtypes = [CT.c_int, CT.c_int, CT.c_int]
        self.__reduce_Sum_int.restype = CT.c_int

        self.__reduce_Sum_double = MPIpy.c_code.reduceSumDouble
        self.__reduce_Sum_double.argtypes = [CT.c_double, CT.c_int, CT.c_int]
        self.__reduce_Sum_double.restype = CT.c_double

        self.__Bcast_int = MPIpy.c_code.mpi_Bcast_int
        self.__Bcast_int.argtypes = [CT.c_void_p,CT.c_int, CT.c_int, CT.c_int]

        self.__Bcast_double = MPIpy.c_code.mpi_Bcast_double
        self.__Bcast_double.argtypes = [CT.c_void_p,CT.c_int, CT.c_int, CT.c_int]

        self.__scatter = MPIpy.c_code.mpi_scatter
        self.__scatter.argtypes = [CT.c_void_p, CT.c_int, CT.c_int, CT.c_void_p, CT.c_int, CT.c_int, CT.c_int]

        self.__gather = MPIpy.c_code.mpi_gather_s
        self.__gather.argtypes = [CT.c_void_p, CT.c_int, CT.c_int, CT.c_void_p, CT.c_int, CT.c_int]

        self.__barrier = MPIpy.c_code.barrier
        self.__barrier.argtypes = [CT.c_int]
        self.__barrier.restype = CT.c_int

        self.__super_free = MPIpy.c_code.super_free
        self.__super_free.argtypes = [CT.c_void_p]

        self.__matmul_double = MPIpy.c_code.matmul_double
        self.__matmul_double.argtypes = [
            CT.c_void_p, CT.c_void_p, CT.c_int,
            CT.c_int, CT.c_int, CT.c_void_p
        ]

        self.__get_processor_name = MPIpy.c_code.mpi_get_processor_name
        self.__get_processor_name.argtypes = [CT.c_void_p]

        self.__reduceChoiceInt = MPIpy.c_code.reduceChoiceInt
        self.__reduceChoiceInt.argtypes = [CT.c_void_p, CT.c_int, CT.c_void_p, CT.c_int, CT.c_int, CT.c_int]
        self.__reduceChoiceInt.restype = CT.c_int

        self.__finalize = MPIpy.c_code.MPI_Finalize

        MPIpy.c_code.MPI_Init()
        self.rank = self.rankf()
        self.size = self.sizef()
        self.sizeS = self.size - 1 # when main is not considered a worker.
        atexit.register(self.__finalize)

    def rankf(self, comm = cworld) -> int:
        """Rank of the individual node."""
        return self.__rank(comm)

    def sizef(self, comm = cworld) -> int:
        """Size of working pool"""
        return self.__size(comm)
    
    def send_int(self, data, dest, tag, comm = cworld) -> None:
        """Sends data over MPI_Send, default only needs 3 arguments."""
        self.__int_send(data, 1, dest, tag, comm)

    def return_int(self, source, tag, comm_m = cworld) -> int:
        """Return data sent over MPI_Send, default only needs 2 arguments"""
        return self.__int_recv(1, source, tag, comm_m)

    def send_int_array(self, data: list, dest: int, tag: int, comm_m = cworld) -> None:
        """Sends a list of integers over mpi."""
        length = len(data)
        parsedData = (CT.c_long * length)(*data)
        self.__array_send_int(parsedData, length, dest, tag, comm_m)

    def send_double_array(self, data: list, dest: int, tag: int, comm_m = cworld) -> None:
        """Sends a list of floats over mpi."""
        length = len(data)
        parsedData = (CT.c_double * length)(*data)
        self.__array_send_double(parsedData, length, dest, tag, comm_m)

    def recv_int_array(self, dataE: list,  source: int, tag: int, comm_m = cworld) -> None:
        """Overwrites a given list of integers sent over mpi."""
        length = self.__array_recv_int(CT.pointer(self.temp_P), source, tag, comm_m)
        test2 = (CT.c_long * length).from_address(self.temp_P.value)
        dataE.extend(test2[::])
        self.__super_free(CT.pointer(self.temp_P))

    def recv_double_array(self, dataE: list, source: int, tag: int, comm_m = cworld) -> None:
        """Overwrites a given list of doubles (floats) sent over mpi."""
        length = self.__array_recv_double(CT.pointer(self.temp_P), source, tag, comm_m)
        test2 = (CT.c_double * length).from_address(self.temp_P.value)
        dataE.extend(test2[::])
        self.__super_free(CT.pointer(self.temp_P))

    def reduceChoiceInt(self, data: int | list[int], choice:str, root: int = 0, comm_m = cworld ) -> list[int]:
        dataRe = []
        try:
            ch = MPIpy.minidict[choice]
        except:
            print("redice choice invalid")
            exit(-1)
        if type(data) == int:
            data = list(data)
        length = len(data)
        parsedData = (CT.c_long * length)(*data)
        self.__reduceChoiceInt(CT.pointer(parsedData),length, CT.pointer(self.temp_P), root, comm_m,  ch)
        if root == self.rank:
            data = (CT.c_long * length).from_address(self.temp_P.value)
            dataRe.extend(data[::])
            self.__super_free(CT.pointer(self.temp_P))
            return dataRe
        else:
            return data

    def reduceSumInt(self, sum, master, comm_m = cworld) -> int:
        """All nodes include their parial sum for it to be added
            all together then returned to everyone what the total sum is."""
        return self.__reduce_Sum_int(sum, master, comm_m)

    def reduceSumDouble(self, sum, master, comm_m=cworld) -> float:
        """All nodes include their partial sum for it to be added
            all together when returned to everyone what the total sum is."""
        return self.__reduce_Sum_double(sum, master, comm_m)
    
    def Bcast_int(self, data: int or list, sender: int, comm_m = cworld) -> None:
        """Use MPI Bcast send over an int or a list of int's to all.
            !!non-senders must always pass in an empty list into data.
        """
        if type(data) == int:
            data = [data]
        temp_ar = 1 * CT.c_int
        temp = temp_ar()
        if self.rank == sender:
            temp[0] = len(data)
            self.__Bcast_int(CT.pointer(temp), 1, sender, comm_m)
            temp_ar = temp[0] * CT.c_int
            temp = temp_ar()
            for i in range(len(data)):
                temp[i] = data[i]
            self.__Bcast_int(CT.pointer(temp), len(data), sender, comm_m)
        else:
            self.__Bcast_int(CT.pointer(temp), 1, sender, comm_m)
            length = temp[0]
            temp_ar2 = length * CT.c_int
            temp2 = temp_ar2()
            self.__Bcast_int(CT.pointer(temp2), length, sender, comm_m)
            for i in range(length):
                data.append(temp2[i])
    
    def Bcast_double(self,data: int or list, sender: int, comm_m = cworld) -> None:
        """Use MPI Bcast send over an int or a list of int's to all.
            !!non-senders must always pass in an empty list into data.
            is a single float is sent over, a list with 2 values, 0.0 being the seccond will return.
        """
        if type(data) == float:
            data = [data,0.0] # python seems to make a mess unless i add the 0.0
        temp_ar = 1 * CT.c_int
        temp = temp_ar()
        if self.rank == sender:
            temp[0] = len(data)
            self.__Bcast_int(CT.pointer(temp), 1, sender, comm_m)
            temp_ar = temp[0] * CT.c_double
            temp = temp_ar()
            for i in range(len(data)):
                temp[i] = data[i]
            self.__Bcast_double(CT.pointer(temp), len(data), sender, comm_m)
        else:
            self.__Bcast_int(CT.pointer(temp), 1, sender, comm_m)
            length = temp[0]
            temp_ar2 = length * CT.c_double
            temp2 = temp_ar2()
            self.__Bcast_double(CT.pointer(temp2), length, sender, comm_m)
            for i in range(length):
                data.append(temp2[i])

    def Scatter(self, dataList: list, sender, comm_m = cworld) -> None:
        """MPI_Scatter for MPIpy."""
        scrap_data = CT.c_int * 2
        scrap = scrap_data()
        scrapP = CT.pointer(scrap)


        if self.rank == sender:
            lengthM = len(dataList)
            lengthS = int(lengthM / self.size)

            if (lengthM % self.size) != 0:
                self.Bcast_int([0, 0, 0], sender)
                print("The data needs to be equally divided among all in the comm.")
                print("Stopped MPI_Scatter")
                return
            
            if type(dataList[0]) == int:
                sType = 1
                temp_ar = lengthM * CT.c_int
                temp = temp_ar()
                for i in range(lengthM):
                    temp[i] = dataList[i]
                mast_ar = lengthS * CT.c_int
                mast = mast_ar()
            elif type(dataList[0]) == float:
                sType = 2
                temp_ar = lengthM * CT.c_double
                temp = temp_ar()
                for i in range(lengthM):
                    temp[i] = dataList[i]
                mast_ar = lengthS * CT.c_double
                mast = mast_ar()
                
            else:
                self.Bcast_int([0, 0, 0], sender, comm_m)
                print("The data was not recognised.")
                print("Stopped MPI_Scatter")
                return

            self.Bcast_int([lengthM, lengthS, sType], sender, comm_m)

            self.__scatter(CT.pointer(temp), lengthS, sType, CT.pointer(mast), lengthS, sender, comm_m)
            # temp_l = dataList[:lengthS]
            dataList.clear()
            for i in range(lengthS):
                dataList.append(mast[i])

        else:
            data = []
            self.Bcast_int(data, sender, comm_m)
            if data[0] == 0:
                print("Was given an error commnd, stopped MPI_Scatter")
                return
            lengthM = data[0]
            lengthS = data[1]
            sType = data[2]
            if sType == 1:
                temp_ar = lengthS * CT.c_int
            if sType == 2:
                temp_ar = lengthS * CT.c_double
            temP = temp_ar()
            temp = CT.pointer(temP)
            self.__scatter(scrapP, lengthS, sType, temp, lengthS, sender, comm_m)
            for i in range(lengthS):
                dataList.append(temp.contents[i])

    def gather(self, dataList: list, reciever = 0, comm_m = cworld) -> None:
        """MPI_Gather for MPIpy."""

        lengthS = len(dataList)

        if type(dataList[0]) == int:
            sType = 1
            temp_ar = lengthS * CT.c_int
            temp = temp_ar()
            for i in range(lengthS):
                temp[i] = dataList[i]

        elif type(dataList[0]) == float:
            sType = 2
            temp_ar = lengthS * CT.c_double
            temp = temp_ar()
            for i in range(lengthS):
                temp[i] = dataList[i]

        self.__gather(CT.pointer(temp), lengthS, sType, CT.pointer(self.temp_P), reciever, comm_m)

        dataList.clear()
        if self.rank == reciever:
            if sType == 1: # int
                tmp2 = (CT.c_int * (lengthS * self.size))
                tmp2 = tmp2.from_address(self.temp_P.value)
                dataList.extend(tmp2[::]) 
        
        self.__super_free(CT.pointer(self.temp_P))

    def Get_processor_name(self, comm = cworld) -> str:
        """Get the name of the processor."""
        # name = CT.create_string_buffer(256)
        self.__get_processor_name(CT.pointer(self.temp_P))
        test2 = (CT.c_char * 256).from_address(self.temp_P.value) # switched to c_char
        self.__super_free(CT.pointer(self.temp_P))
        return test2.value  # changed to test2.value

    def barrier(self, comm_m = cworld) -> None:
            """MPI_Barrier"""        
            self.__barrier(comm_m)
    
    def matmulC(self, LA: list, LB: list, rowA: int, shareB: int, colC: int, LC: list) -> None:
        """Uses a simple matrix algorithm but in c... so its allot faster.
            LA[rowA][shareB]
            LB[shareB][colC]
            LC[rowA][colC] this needs to be an empty python list to append to.   
        """
  
        lengthA = rowA * shareB
        lengthB = shareB * colC
        lengthC = rowA * colC
        parsedDataA = (CT.c_double * lengthA)(*LA)
        parsedDataB = (CT.c_double * lengthB)(*LB)
        self.__matmul_double(
            parsedDataA, parsedDataB, 
            rowA, shareB, colC,
            CT.pointer(self.temp_P),
            )
        test2 = (CT.c_double * lengthC).from_address(self.temp_P.value)
        LC.extend(test2[::])
        self.__super_free(CT.pointer(self.temp_P))

if __name__ == "__main__":
    MPI = MPIpy()
    test = [float(a) for a in range(4*4)]
    test2 = test[::]
    test3 = []
    MPI.matmulC(test, test2, 4, 4, 4, test3)
    for a in range(4):
        for b in range(4):
            print(test3[a * 4 + b], end=", ")
        print()
    key = [ # the output should look like this.
        56, 62, 68, 74,
        152, 174, 196, 218,
        248, 286, 324, 362,
        344, 398, 452, 506,
    ]
