
//package a2;

import java.util.*;

public class L1a {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		
		int N = sc.nextInt();
		int l = sc.nextInt();
		
		ArrayList<BinaryString> binaryStrings = new ArrayList<BinaryString>();
		ArrayList<Double> u = new ArrayList<Double>();
		
		// Reads all the BinaryString and respective fitness values
		for(int i = 0; i<N; i++){
			BinaryString binaryString = new BinaryString();
			binaryString.setBinaryString(sc.next() + "");
			binaryString.setFitness(sc.nextDouble());
			binaryStrings.add(binaryString);
		}
		
		// Reads all the random values
		for(int i = 0; i<N; i++){
			u.add(sc.nextDouble());
		}
		
		RouletteWheel rouletteWheel = new RouletteWheel();
				
		// runs N RouletteWheels
		for(int i=0; i<N; i++){
						
			// select and prints the competition winner
			BinaryString winner = rouletteWheel.run(binaryStrings, u.get(i));
			System.out.println(winner.getBinaryString());
		}

	}

}

class RouletteWheel{
	// O(3*n)
	public BinaryString run(ArrayList<BinaryString> binaryStrings, double u){
		
		// assert u is a random number
		assert(u>=0);
		assert(u<1);
		
		// Wheel Distribution list
		ArrayList<Double> wheelDistribution = generateWheelDistribution(binaryStrings);
		
		// Searches for the slice corresponding to the given random number;
		int index = 0;
		Iterator<Double> iterator = wheelDistribution.iterator();
		while(iterator.hasNext()){
			// return the element corresponding to the currrent slice if the random number is bigger than the current slice limit, but wasn't bigger than the previous
			if(u<iterator.next())
				return binaryStrings.get(index);
			else
				index++;
		}
		return null;
		
	}
	
	// Given a list of BinaryStrings (with corresponding fitnesses), generates the ordered list with the corresponding wheel "slice" for each element. 
	// Offset taken into consideration so, each element corresponds to the maximum value between 0 and 1 that belongs to the indexed "slice"
	// Ex: if element 1 has p=0.15 and element 2 has p=0.20... The list will have at least 3 elements, being the first two 0.15 and 0.35. Where 0.35 = 0.15 + 0.20.
	private ArrayList<Double> generateWheelDistribution (ArrayList<BinaryString> binaryStrings){
		
		// Stores each slice value (slice probability + all the previous slices probabilities)
		ArrayList<Double> wheelDistribution = new ArrayList<Double>();
		
		// Calculates Total Fitness
		double totalFitness = calculateTotalFitness(binaryStrings);
		
		// Offset accumulator
		double offset = 0;
		
		// Computes the wheelDistribution considering each element
		Iterator<BinaryString> iterator = binaryStrings.iterator();
		while(iterator.hasNext()){
			offset += iterator.next().getFitness() / totalFitness;
			wheelDistribution.add(offset);
		}
		
		// The offset value has to be exactly 1 uppon accumulating all the elements.
		assert(offset==1);
		
		return wheelDistribution;
	}
	
	// Calculates the sum of the fitness of all elements (basic accumulation function)
	private Double calculateTotalFitness (ArrayList<BinaryString> binaryStrings){
		double totalFitness = 0;
		Iterator<BinaryString> iterator = binaryStrings.iterator();
		while(iterator.hasNext()){
			totalFitness += iterator.next().getFitness();
		}
		
		return totalFitness;
	}
	
}

class  Tournament{
	
	// Runs the tournament for the elements on binaryStrings list, randomly chooses u.size elements, and from those winner elements choose the fittest as the ultimate winner
	public BinaryString runTournament(ArrayList<BinaryString> binaryStrings, ArrayList<Double> u){
		ArrayList<BinaryString> randomBinaryStrings = new ArrayList<BinaryString>();
		
		// Selects u.size random binaryStrings
		for(int i = 0; i<u.size(); i++){
			randomBinaryStrings.add(this.selectRandomBinaryString(binaryStrings, u.get(i)));
		}
		
		return this.selectFittestBinaryString(randomBinaryStrings);
		
	}
	
	// Recieves a random number and the smallest and biggest index numbers, and returns a selected one
	public int selectRandom(int a, int b, double u){
		return (int)(a + (Math.floor(u * (double)(b - a + 1))));
	}
	
	// Recieves a list of BinaryString and a random number and returns the selected BinaryString
	public BinaryString selectRandomBinaryString(ArrayList<BinaryString> binaryStrings, double u){
		int index = this.selectRandom(1,binaryStrings.size(), u);
		return binaryStrings.get(index-1);
	}
	
	// Recieves a non-empty list of BinaryString and returns the fittest element
	public BinaryString selectFittestBinaryString(ArrayList<BinaryString> binaryStrings){
		Iterator<BinaryString> iterator = binaryStrings.iterator();

		// If the list is not empty search for the fittest element
		if(!binaryStrings.isEmpty()){
			BinaryString fittest, current;
			fittest = current = iterator.next();
			
			// while there is elementes on the list, update the fittest
			while(iterator.hasNext()){
				current = iterator.next();
				if(current.getFitness() > fittest.getFitness())
					fittest = current;
			}
			
			return fittest;

			
		}else // if the list is empty return null
			return null;
	}


}
class BinaryString {
	
	private String binaryString;
	private double fitness;
	
	public String getBinaryString() {
		return binaryString;
	}
	public void setBinaryString(String binaryString) {
		this.binaryString = binaryString;
	}
	public double getFitness() {
		return fitness;
	}
	public void setFitness(double d) {
		this.fitness = d;
	}
}

class BinaryStringManipulator {
	
	// Recieves a list with l random numbers and generates a binary string from those numbers
	public String generate(List<Double> u){
		
		// the default binary string
		String binaryString = "";
		
		// while there is random numbers, iterate through them (there should be l of them)
		Iterator<Double> iterator = u.iterator();
		double current;
		while(iterator.hasNext()){
			
			// gets the next random number
			current = iterator.next();
			
			// if the random number is bigger less than 0.5 add 1 to the binary string, else add 0
			if(current < 0.5)
				binaryString = binaryString + "0";
			else
				binaryString = binaryString + "1";
		}
		
		// return the binaryString
		return binaryString;		
	}
	
	// Recieves a binaryString and calculates the number of 1s in that String
	public int onemax(String binaryString, int length){
		int count=0;
		
		for(int i = 0; i<length;i++)
			if(binaryString.charAt(i) == '1')
				count++;
		
		return count;
	}

	// Converts the binaryString into a decimal number
	public int binaryStringtoDecimal(String binaryString, int length){
		int result = 0;
		
		for(int i =0; i<length; i++)
			result+=Math.pow(2, i) * Integer.parseInt(binaryString.charAt(length-i-1)+"");
		
		return result;
	}

	// calculates the fitness of a binaryString based on the function f(x)=x^2
	public int fitSquareX(String binaryString, int length){
		return (int) Math.pow(this.binaryStringtoDecimal(binaryString,length),2);
	}

}