
//package d2;

import java.util.*;

public class Main {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		
		int l = sc.nextInt();
		
		BinaryString binaryString1 = new BinaryString();
		BinaryString binaryString2 = new BinaryString();

		ArrayList<Double> u = new ArrayList<Double>();
		
		// Reads the BinaryStrings
		binaryString1.setBinaryString(sc.next() + "");
		binaryString2.setBinaryString(sc.next() + "");

		// Reads the random values
		for(int i=0;i<l;i++)
			u.add(sc.nextDouble());
		
		CrossOver co = new CrossOver();
		ArrayList<String> childs = co.uniformCrossOver(binaryString1.getBinaryString(), binaryString2.getBinaryString(), u);
		System.out.println(childs.get(0));
		System.out.println(childs.get(1));
		
	}
		

}

class CrossOver{
	
	// Given two BinaryStrings of size l, and l random numbers returns the reults of uniformCrossOver for those binaryStrings (which is another pair of binaryStrings)
	public ArrayList<String> uniformCrossOver (String bsX, String bsY, ArrayList<Double> u){
		
		// String manipulator
		BinaryStringManipulator bsm = new BinaryStringManipulator();
		
		// Generates pattern r and the inverse r_
		String r 	= this.patternGenerator(u);
		/*String r_ 	= bsm.NOT(r);
		
		// child 1 from X and Y = r_.x ^ r.y   (. is the "AND" operation and ^ the "XOR")
		// child 2 from X and Y = r.x  ^ r_.y  (. is the "AND" operation and ^ the "XOR") 
		String bsXY1 = bsm.XOR(bsm.AND(bsX, r), bsm.AND(bsY, r_)); 
		String bsXY2 = bsm.XOR(bsm.AND(bsY, r), bsm.AND(bsX, r_));
		
		
		// Store and return the resulting children from the crossover
		ArrayList<String> childs = new ArrayList<String>();
		childs.add(bsm.missingZeros(bsXY1, r.length()));
		childs.add(bsm.missingZeros(bsXY2, r.length()));
		
		return childs;*/
		
		return uniformChilds(bsX,bsY,r);
	}
	
	// Given a list of random numbers returns the crossover application pattern
	public String patternGenerator(ArrayList<Double> u){
		BinaryStringManipulator bsm = new BinaryStringManipulator();
		return bsm.generate(u);
	}
	
	public ArrayList<String> uniformChilds (String bsX, String bsY, String r){
		// Initialize childs
		String bsX_="";
		String bsY_ = "";
		
		// In positions where pattern r has the value 0, keep bits the same as corresponding parent
		// 						where r has the value 1, swap the bit with the other parent
		for(int i=0; i<r.length(); i++)
			if(r.charAt(i)=='1'){
				//swap bits
				bsX_ += bsY.charAt(i);
				bsY_ += bsX.charAt(i);
			}else{
				//maintain bits
				bsX_ += bsX.charAt(i);
				bsY_ += bsY.charAt(i);
			}
		
		// Store and return the resulting children from the crossover
		ArrayList<String> childs = new ArrayList<String>();
		childs.add(bsY_);
		childs.add(bsX_);
		
		return childs;
	}
}

class Mutation{
	
	// Computes the mutation of a binaryString bit by bit given the probability of mutation for all the bits and a random value for each bit 
	public String binaryStringMutation(String binaryString, double pMut, ArrayList<Double> u){
		
		// The number of random numbers provided must be equal to the number of bits on the binaryString
		assert(binaryString.length() == u.size());
		
		// Instanciate manipulator
		BinaryStringManipulator bsm = new BinaryStringManipulator();
		
		// The returning bynaryString
		String binaryString_mutated = binaryString;
		
		// for all the bits in the string, check if the corresponding random value is less than the probability of mutation, if so mutate the bit.
		for(int i=0; i<binaryString.length();i++)
			if(u.get(i)<pMut)
				binaryString_mutated = bsm.FLIP(binaryString_mutated, i);
		
		return binaryString_mutated;
		
	}
}


// Stochastic Universal Sampling
class SUS{
	// u is a random number and numberElements is the number of elements to keep
	public ArrayList<BinaryString> run(ArrayList<BinaryString> binaryStrings, double u, int numberElements){
		
		// List containing the selected elements
		ArrayList<BinaryString> selected = new ArrayList<BinaryString>();
		
		// Wheel Distribution list
		ArrayList<Double> wheelDistribution = generateWheelDistribution(binaryStrings);
		
		// Calculates population total fitness
		double totalFitness = calculateTotalFitness(binaryStrings);
		
		// Calculates the distance between the pointers (equal distance between all points) 
		double pointerDistance = totalFitness/numberElements;
		
		// Chooses the starting point of the random points
		double startingPointer = randomMap(0,pointerDistance,u);
		
		double pointerDistanceScaled = pointerDistance/totalFitness;
		double startingPointerScaled = startingPointer/totalFitness;
		
		// Searches the wheel distribution for the element corresponding to each point
		Iterator<Double> iterator = wheelDistribution.iterator();
		
		int pointerNumber = 0;
		double currentPointer = startingPointerScaled;
		int index = 0;
		
		double currentWheel = iterator.next();
		while(iterator.hasNext() && pointerNumber <= numberElements ){
			// add the element corresponding to the currrent slice if the random number is bigger than the current slice limit, but wasn't bigger than the previous
			if(currentPointer<currentWheel){
				 selected.add(binaryStrings.get(index));
				 pointerNumber++;
				 currentPointer += pointerDistanceScaled; 
			}else{
				currentWheel = iterator.next();
				index++;
			}
		}
				
		return selected;
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

	// Recieves a random number and the smallest and biggest index numbers, and returns a selected one
	private double randomMap(double a, double b, double u){
		return u * (double)(b - a);
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

}

// Can be improved, generating the Wheel Distribution can be done only once for all the population,
// it's not necessary to generate it every time the wheel is runned
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
		while(iterator.hasNext())
			// return the element corresponding to the currrent slice if the random number is bigger than the current slice limit, but wasn't bigger than the previous
			if(u<iterator.next())
				return binaryStrings.get(index);
			else
				index++;
		
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

class Tournament{
	
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
	// Random number u represents a 0 if u < 0.5 or a 1 if u>=0.5
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
	
	// Recieves a binaryString and calculates the number of 1 bits
	public int onemax(String binaryString, int length){
		int count=0;
		
		for(int i = 0; i<length;i++)
			if(binaryString.charAt(i) == '1')
				count++;
		
		return count;
	}

	// Converts a binaryString into a decimal number
	public int binaryStringtoDecimal(String binaryString){
		int result = 0;
		int length = binaryString.length();
		for(int i =0; i<length; i++)
			result+=Math.pow(2, i) * Integer.parseInt(binaryString.charAt(length-i-1)+"");
		
		return result;
	}

	// Converts a decimal number into a binaryString
	public String decimaltoBinaryString(int value){
		return Integer.toBinaryString(value);
	}
		
	// Calculates the fitness of a binaryString based on the function f(x)=x^2
	public int fitSquareX(String binaryString, int length){
		return (int) Math.pow(this.binaryStringtoDecimal(binaryString),2);
	}
	
	// bitwise operation "BIT FLIP" (not actually computed with bitwise operators)
	public String FLIP(String binaryString, int index){
		
		// store the sub strings of bits preceding and proceeding the bit we want to flip 
		String pre = binaryString.substring(0, index);
		String post = binaryString.substring(index+1, binaryString.length());
		
		// return the concatenation of the preceding substring, the fliped bit and the proceeding substring of bits
		if(binaryString.charAt(index)=='0')
			return pre + "1" + post;
		else
			return pre + "0" + post;
	}
	
	// bitwise operation "AND"
	public String AND(String binaryString1,String binaryString2){
		// Convert Strings to decimal
		int binaryString1_10 = binaryStringtoDecimal(binaryString1);
		int binaryString2_10 = binaryStringtoDecimal(binaryString2);
		
		// Realize bitwise operation
		int bsResult_10 = binaryString1_10 & binaryString2_10;
		
		// Return corresponding String
		return decimaltoBinaryString(bsResult_10);
	}

	// bitwise operation "OR"
	public String OR(String binaryString1,String binaryString2){
		// Convert Strings to decimal
		int binaryString1_10 = binaryStringtoDecimal(binaryString1);
		int binaryString2_10 = binaryStringtoDecimal(binaryString2);

		// Realize bitwise operation
		int bsResult_10 = binaryString1_10 | binaryString2_10;

		// Return corresponding String
		return decimaltoBinaryString(bsResult_10);
	}

	// bitwise operation "exclusive-OR"
	public String XOR(String binaryString1,String binaryString2){
		// Convert Strings to decimal
		int binaryString1_10 = binaryStringtoDecimal(binaryString1);
		int binaryString2_10 = binaryStringtoDecimal(binaryString2);

		// Realize bitwise operation
		int bsResult_10 = binaryString1_10 ^ binaryString2_10;

		// Return corresponding String
		return decimaltoBinaryString(bsResult_10);
	}

	// bitwise operation "NOT"
	public String NOT(String binaryString){
		
		String result = "";
		
		for(int i = 0; i<binaryString.length();i++)
			
			if(binaryString.charAt(i)=='0')
				result+= "1";
			else
				result+="0";
		return result;
	}

	// bitwise operation "SHIFT LEFT"
	public String SHIFT_L(String binaryString, int n){
		// Convert String to decimal
		int binaryString_10 = binaryStringtoDecimal(binaryString);

		// Realize bitwise operation
		int bsResult_10 = binaryString_10 << n;

		// Return corresponding String
		return decimaltoBinaryString(bsResult_10);
	}
	
	// bitwise operation "SHIFT RIGHT"
	public String SHIFT_R(String binaryString, int n){
		// Convert String to decimal
		int binaryString_10 = binaryStringtoDecimal(binaryString);

		// Realize bitwise operation
		int bsResult_10 = binaryString_10 >> n;

		// Return corresponding String
		return decimaltoBinaryString(bsResult_10);
	}

	// adds missing zeros to the binary String so it gets length N 
	public String missingZeros(String binaryString, int n){
		
		// calculates number of zeros nissing
		int numberMissingZeros = n - binaryString.length();
		String missingZeros = "";
		
		// creates a string with all the zeros missing
		for(int i = 0; i<numberMissingZeros; i++)
			missingZeros += "0";
		
		// returns the concatenation of the missing zeros with the binaryString
		return missingZeros + binaryString;
	}
	

}