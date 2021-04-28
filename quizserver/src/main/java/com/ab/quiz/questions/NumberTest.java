package com.ab.quiz.questions;

import java.util.ArrayList;
import java.util.List;

public class NumberTest {
	public static void main(String[] args) {
		
		NumberTest test1 = new NumberTest();
		List<Integer> primeNumbers = test1.getPrimeNums(2,100);
		int primseNumsListSize = primeNumbers.size();
		System.out.println("primeNumbers.size " + primseNumsListSize);
		System.out.println(primeNumbers);
		List<Integer> subList = primeNumbers.subList(0, 16);
		long lcmResult = test1.findLCM(subList);
		System.out.println(lcmResult);
		
		int divCount = 0;
		for (int index = 0; index < primeNumbers.size(); index++) {
			if ((lcmResult % primeNumbers.get(index)) == 0) {
				divCount++;
			}
		}
		System.out.println(divCount);
	}
	
	private List<Integer> getPrimeNums(int start, int end) {
		List<Integer> primes = new ArrayList<>();
		int i, j, flag;
		
		for (i = start; i <= end; i++) {
			 
            // Skip 0 and 1 as they are
            // niether prime nor composite
            if (i == 1 || i == 0)
                continue;
 
            // flag variable to tell
            // if i is prime or not
            flag = 1;
 
            for (j = 2; j <= i / 2; ++j) {
                if (i % j == 0) {
                    flag = 0;
                    break;
                }
            }
 
            // flag = 1 means i is prime
            // and flag = 0 means i is not prime
            if (flag == 1) {
            	primes.add(i);
            }
        }
		return primes;
	}
	
	private long findLCM(List<Integer> nums) {
		long lcm = nums.get(0);
		long gcd = nums.get(0);

		// loop through the array to find GCD
		// use GCD to find the LCM
		for (int i = 1; i < nums.size(); i++) {
			gcd = findGCD(nums.get(i), lcm);
			lcm = (lcm * nums.get(i)) / gcd;
		}
		return lcm;
	}
	
	public static long findGCD(long a, long b) {
		// base condition
		if (b == 0)
			return a;
		return findGCD(b, a % b);
	}

}
