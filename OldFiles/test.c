#include <stdio.h>

int main(int argc, char *argv[])
{
    int tmp = 0;
    int stp1 = 40 / 7 ;
    int stp2 = 40 % 7 ;
    for (int i = 0; i < 7; i++)
    {
        printf("%d\n", stp1 + (i<stp2) ) ; 
    }
}