//only stores information, does no calculations or anything
//used to store info about tennants utilities
//name, apartment, water/electricity  usage, water/electricity cost
public class Record {
	private String name;
	private String apartment;
	private double waterUsage;
	private double electricityUsage;
	private double waterCost;
	private double electricityCost;

	Record(String aName, String anApartment, double aWaterUsage, double anElectricityUsage, double aWaterCost, double anElectricityCost) {
		name = aName;
		apartment = anApartment;
		waterUsage = aWaterUsage;
		electricityUsage = anElectricityUsage;
		waterCost = aWaterCost;
		electricityCost = anElectricityCost;
	}
	public String getName(){return name;}
	public String getApartments(){return apartment;}
	public double getWaterUsage(){return waterUsage;}
	public double getElectricityUsage(){return electricityUsage;}
	public double getWaterCost(){return waterCost;}
	public double getElectricityCost(){return electricityCost;}
}
