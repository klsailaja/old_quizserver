package com.ab.quiz.questions;

public class CombinationNonRecursive
{
    static int count=0;
    public static void main(String[] args)
    {
        char[] values = {'r','f','c','e', 'e', 'r'};
        int n = values.length;
        int r = 6;
        int output[] = new int[r];
 
        for(int numIterations=0; numIterations<Math.pow(n,r); numIterations++)
        {
            print(values, r, output);
            int index = 0;
            while(index < r)
            {
                if(output[index] < n-1)
                {
                    output[index]++;
                    break;
                }
                else
                {
                    output[index]=0;
                }
                index++;
            }
        }
    }
 
    private static void print(char[] values, int r, int[] output)
    {
        System.out.print(String.format("\n%2d", ++count) + ") ");
        while(r-- > 0)
        {
            System.out.print(values[output[r]]);
        }
    }
}
