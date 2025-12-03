/**
 * 
 */

import java.util.Scanner;

/**
 * 
 */
public class CarLoanCalculator {


	public static double CalculateLoan (double carPrice,double deposit ) {
		
		return carPrice-deposit;
		
	}
	
	public static double MonthlyPayment(double loan,double annualRate,int years)
	{
		double monthlyRate=(annualRate/100)/12;
		int months=years*12;
		
		if(monthlyRate==0) {
			
			return loan/months;
			
		}
		double numerator = loan * monthlyRate * Math.pow(1 + monthlyRate, months);
        double denominator = Math.pow(1 + monthlyRate, months) - 1;

        return numerator / denominator;
	
		
	}
	
	public static double CalcTotalPayments(double monthlyPayments,int years)
	
	{
		double total = monthlyPayments * years * 12;
        return Math.max(total, 0);
		
	}
	
	 // Total interest paid
    public static double calculateTotalInterest(double totalPayment,
                                                 double loanAmount) {
    	 double interest = totalPayment - loanAmount;
         return Math.max(interest, 0);
    }
	
    // Validation
    public static boolean isValid(double carPrice,
                                  double deposit,
                                  double interestRate,
                                  int years) {

        if (carPrice <= 0) return false;
        if (deposit < 0 || deposit > carPrice) return false;
        if (interestRate < 0) return false;
        if (years <= 0) return false;

        return true;
    }

}
