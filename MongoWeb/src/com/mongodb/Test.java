package com.mongodb;

public class Test {
public static void main(String args[]){
	
	  int i = 0;
      for (int bit = 0; bit < 16; bit++) {
          i |= bit << bit;
      }
      System.out.println("Test.main()"+Integer.toString(i));
    
}
}
