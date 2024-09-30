#include <stdlib.h>
#include <stdio.h>
#include "mpi.h"
/* to compile:
 * $ mpicc 'comp_MPIpy copy.c' -shared -fPIC -o comp_MPIpy.so
 * If the line above does not work, try this one:
 * $ mpicc.mpich 'comp_MPIpy.c' -shared -fPIC -o comp_MPIpy.so
 * Put together by Judah Nava and Elkana Munganga
*/

// Table of Contents
//   MPI_Init(); might need (NULL, NULL) in the parameters.
//   MPI_Finalize();
int  communicator();
int  mpi_comm_rank(int comm);
int  mpi_comm_size(int comm);
void mpi_send_int(int data, int count, int dest, int tag, int comm);
int  mpi_recv_int(int count, int source, int tag, int comm);
void mpi_send_int_array(long* data, int length, int dest, int tag, int comm);
int  mpi_recv_int_array(long ** test, int source, int tag, int comm);
void mpi_send_double_array(double* data, int length, int dest, int tag, int comm);
int  mpi_recv_double_array(double ** data, int source, int tag, int comm);
void super_free(void** ptr);
int  reduceSum(int data, int master, int comm);
int  reduceSumDouble(double data, int master, int comm);
void mpi_Bcast_int(int* buffer, int count, int root, int comm);
void mpi_Bcast_double(double* buffer, int count, int root, int comm);
void mpi_scatter(const void* sendbuf, int sendcount, int datatypeKey, 
                       void* recvbuf, int recvcount, int root, int comm);
void mpi_gather_s(void* sendbuf, int count, int datatypeKey, void** recvbuf, int root, int comm) ;
int  barrier(int comm);
void matmul_double(double* MA, double* MB, int rowA, int shareB, int colC, double** LC);
void mpi_get_processor_name(char ** name);
int reduceChoiceInt(long*, int, long**, int, int, int);

int communicator()
{
    return (int) MPI_COMM_WORLD;
}

int mpi_comm_rank(int comm)
{
    int rank;
    MPI_Comm_rank(comm, &rank);
    return rank;
}

int mpi_comm_size(int comm)
{
    int size;
    MPI_Comm_size(comm, &size);
    return size;
}

void mpi_send_int(int data, int count, int dest, int tag, int comm)
{
    MPI_Send(&data, count, MPI_INT, dest, tag, comm);
}

int mpi_recv_int(int count, int source, int tag, int comm)
{
    int value;
    MPI_Recv(&value, count, MPI_INT, source, tag, comm, MPI_STATUS_IGNORE);
    return value;
}

void mpi_send_int_array(long* data, int length, int dest, int tag, int comm)
{
	MPI_Send(&length, 1, MPI_INT, dest, tag, comm);
	MPI_Send(&data[0], length, MPI_LONG, dest, tag, comm);
}

int mpi_recv_int_array(long ** test, int source, int tag, int comm)
{
	int length;
	MPI_Recv(&length, 1, MPI_INT, source, tag, comm, MPI_STATUS_IGNORE);
	long* data2 = (long*) malloc((length) * sizeof(long));
	MPI_Recv(&data2[0], length, MPI_LONG, source, tag, comm, MPI_STATUS_IGNORE);
	*test = data2;
	return length;
}

void mpi_send_double_array(double* data, int length, int dest, int tag, int comm)
{
	MPI_Send(&length, 1, MPI_INT, dest, tag, comm);
	MPI_Send(&data[0], length, MPI_DOUBLE, dest, tag, comm);
}

int mpi_recv_double_array(double ** data, int source, int tag, int comm)
{
	int length;
	MPI_Recv(&length, 1, MPI_INT, source, tag, comm, MPI_STATUS_IGNORE);
	double* data2 = (double*) malloc((length) * sizeof(double));
	MPI_Recv(&data2[0], length, MPI_DOUBLE, source, tag, comm, MPI_STATUS_IGNORE);
	*data = data2;
	return length;
}

void super_free(void** ptr)
{
	free(*ptr);
}

int reduceSum(int data, int master, int comm)
{
	int dataP = data;
	int finalP;
	MPI_Reduce(&dataP, &finalP, 1, MPI_INT, MPI_SUM, master, comm);
	return finalP;
}

int reduceSumDouble(double data, int master, int comm)
{
	double dataP = data;
	double finalP;
	MPI_Reduce(&dataP, &finalP, 1, MPI_DOUBLE, MPI_SUM, master, comm);
	return finalP;
}

int reduceChoiceInt(long *dataS,int size, long **dataR, int master, int comm, int choice)
/*
* data comes presized
*/
{	
	MPI_Op op;
	switch(choice)
{
	case 1:
		op = MPI_MAX ;
		break;
	case 2:
		op = MPI_MIN ;
		break;
	case 3:
		op = MPI_SUM ;
		break;
	case 4:
		op = MPI_PROD ;
		break;
	case 5:
		op = MPI_LAND ;
		break;
	case 6:
		op = MPI_LOR ;
		break;
	case 7:
		op = MPI_BAND ;
		break;
	case 8:
		op = MPI_BOR ;
		break;
	default:
		MPI_Abort((MPI_Comm) comm, 1) ;
		return -1;
		
}	
	long *test = NULL ;
	if (master == 0)
		test = (long*) malloc (sizeof(long) * size) ;
	MPI_Reduce(&dataS[0], &test[0], size, MPI_LONG, op, master, comm) ;
	*dataR = test ;
	return 0;

}

void mpi_Bcast_int(int* buffer, int count, int root, int comm)
{
	MPI_Bcast(buffer, count, MPI_INT, root, comm);
}

void mpi_Bcast_double(double* buffer, int count, int root, int comm)
{
	MPI_Bcast(buffer, count, MPI_DOUBLE, root, comm);
}

void mpi_scatter(const void* sendbuf, int sendcount, int datatypeKey, void* recvbuf, int recvcount, int root, int comm)
{
	MPI_Datatype data;
	if (datatypeKey == 1)
	{
		data = MPI_INT;
	}
	else if (datatypeKey == 2)
	{
		data = MPI_DOUBLE;
	}
	MPI_Scatter(sendbuf, sendcount, data, recvbuf, recvcount, data, root, comm);
}

	
//Needed to switch to super pointer for
void mpi_gather_s(void* sendbuf, int count, int datatypeKey, void** recvbuf, int root, int comm)
{
	MPI_Datatype data;
	void *recv = NULL;
	int tcp ;
	MPI_Comm_size((MPI_Comm) comm, &tcp) ;
	if (datatypeKey == 1)
	{
		data = MPI_INT;
		recv = malloc(sizeof(long) * count * tcp) ; //! Be causious
		
	}
	else if (datatypeKey == 2)
	{
		data = MPI_DOUBLE;
		recv = malloc(sizeof(double) * count * tcp) ;
		
	}

	MPI_Gather(sendbuf, count, data, recv, count, data, root, comm);
	// int tmp;
	// MPI_Comm_rank(comm, &tmp) ;
	// if (tmp == 0)
	// {

	// }
	*recvbuf = recv ;
}

int barrier(int comm)
{
	MPI_Barrier( comm);
}

void matmul_double(double* MA, double* MB, int rowA, int shareB, int colC, double** LC)
{
    // smallest possible and clearest to use, to use:
    //      MA[A][B]
    //      MB[B][C]
    //      MC[A][C]
    double* tmpLC = (double*) malloc((rowA * colC) * sizeof(double));
    double sum = 0.0;
    int i, j, k;
    for (i = 0; i < rowA; i++)
    {
        for (j = 0; j < shareB; j++)
        {
            for (k = 0; k < colC; k++)
            {
                sum = sum + (MA[i * shareB + k] * MB[k * colC + j]);
            }
			// printf("%.0f, ", sum);
            tmpLC[i * colC + j] = sum;
            sum = 0.0;
        }
    }
    *LC = tmpLC;
}

void mpi_get_processor_name(char ** name)
{
    int len = 256;
	char* namev = (char*) malloc((len) * sizeof(char));
    MPI_Get_processor_name(namev, &len); //added the & - Judah
	*name = namev ;
}
