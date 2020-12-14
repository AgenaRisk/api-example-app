package example;

import com.agenarisk.api.model.DataSet;
import com.agenarisk.api.model.Model;
import com.agenarisk.api.model.Network;
import com.agenarisk.api.model.Node;
import java.util.Arrays;

/**
 * This is an example of building the Car Costs model from scratch and covers:<br>
 * • Adjusting calculation simulation settings<br>
 * • Creating nodes with probability tables, expressions and partitioned expressions and connecting nodes together<br>
 * • Setting basic observations and calculating the model<br>
 * • Retrieving summary statistic result<br>
 * <br>
 * The example can be found in AgenaRisk Desktop Model Library/Introductory/Car Costs/Car Costs.ast
 * 
 * @author Eugene Dementiev
 */
public class CarCosts {
	public static void main(String[] args) throws Exception {
		Model model = Model.createModel();
		
		// Default settings would be: 50, 0.001, 1.0, 5
		model.getSettings().setIterations(50);
		model.getSettings().setConvergence(0.01);
		model.getSettings().setTolerance(1);
		model.getSettings().setSampleSize(5);
				
		Network net = model.createNetwork("Car Costs");
		
		Node carType = net.createNode("car_type", "Car Type", Node.Type.Ranked);
		carType.setStates(Arrays.asList("Small", "Medium", "Large"));
		// Car Type has a uniform distribution, so no need to set the node probability table
		
		Node reliability = net.createNode("reliability", "Reliability", Node.Type.Ranked);
		reliability.setStates(Arrays.asList("High", "Medium", "Low"));
		// Reliability has a uniform distribution, so no need to set the node probability table
		
		Node milesPerGallon = net.createNode("miles_per_gallon", "Miles per gallon", Node.Type.ContinuousInterval);
		milesPerGallon.convertToSimulated();
		milesPerGallon.linkFrom(carType);
		milesPerGallon.setTableFunctions(Arrays.asList(
			// Columns in the table will be in the same order as partition parent state combinations, e.g. Small | Medium | Large
			"TNormal(35, 50, 5, 100)",
			"TNormal(28, 50, 5, 100)",
			"TNormal(18, 30, 5, 100)"
			), Arrays.asList(carType));
		
		Node maintainability = net.createNode("maintainability", "Maintainability", Node.Type.Ranked);
		maintainability.linkFrom(carType);
		maintainability.linkFrom(reliability);
		maintainability.setStates(Arrays.asList("High", "Medium", "Low"));
		maintainability.setTableFunction("TNormal(wmean(2.0,car_type,1.0,reliability), 0.01, 0, 1)");
		
		Node annualMaintenanceCost = net.createNode("total_maintenance_cost", "Annual maintenance cost ($)", Node.Type.ContinuousInterval);
		annualMaintenanceCost.convertToSimulated();
		annualMaintenanceCost.linkFrom(maintainability);
		annualMaintenanceCost.setTableFunctions(Arrays.asList(
			// Columns in the table will be in the same order as partition parent state combinations, e.g. High | Medium | Low
			"TNormal(100, 100, 0, 600)",
			"TNormal(200, 150, 0, 600)",
			"TNormal(500, 150, 0, 600)"
		), Arrays.asList(maintainability));
		
		Node fuelPrice = net.createNode("fuel_price", "Fuel price $ (gallon)", Node.Type.ContinuousInterval);
		fuelPrice.convertToSimulated();
		fuelPrice.createVariable("fuel_price_const", 3);
		fuelPrice.setTableFunction("Arithmetic(fuel_price_const)");
		
		Node pricePerMile = net.createNode("price_per_mile", "Price per mile $", Node.Type.ContinuousInterval);
		pricePerMile.convertToSimulated();
		pricePerMile.linkFrom(fuelPrice);
		pricePerMile.linkFrom(milesPerGallon);
		pricePerMile.setTableFunction("Arithmetic(fuel_price/miles_per_gallon)");
		
		Node milesPerYear = net.createNode("miles_per_year", "Miles per year", Node.Type.ContinuousInterval);
		milesPerYear.convertToSimulated();
		milesPerYear.createVariable("miles_per_year_const", 10000);
		milesPerYear.setTableFunction("Arithmetic(miles_per_year_const)");
		
		Node annualFuelCost = net.createNode("total_fuel_cost", "Annual fuel cost $", Node.Type.ContinuousInterval);
		annualFuelCost.convertToSimulated();
		annualFuelCost.linkFrom(milesPerYear);
		annualFuelCost.linkFrom(pricePerMile);
		annualFuelCost.setTableFunction("Arithmetic(price_per_mile*miles_per_year)");
		
		Node totalAnnualCost = net.createNode("total_cost", "Total annual cost $", Node.Type.ContinuousInterval);
		totalAnnualCost.convertToSimulated();
		totalAnnualCost.linkFrom(annualFuelCost);
		totalAnnualCost.linkFrom(annualMaintenanceCost);
		totalAnnualCost.setTableFunction("Arithmetic(total_fuel_cost+total_maintenance_cost)");

		DataSet ds = model.createDataSet("Honda");		
		ds.setObservation(carType, "Medium");
		ds.setObservation(reliability, "High");
		
		model.calculate();
		
		System.out.println(ds.getCalculationResult(totalAnnualCost).getMean());
		
	}
}
