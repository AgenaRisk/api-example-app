package example.legacy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import uk.co.agena.minerva.model.Model;
import uk.co.agena.minerva.model.extendedbn.*;
import uk.co.agena.minerva.model.scenario.Scenario;
import uk.co.agena.minerva.util.model.DataPoint;
import uk.co.agena.minerva.util.model.DataSet;
import uk.co.agena.minerva.model.MarginalDataItem;
import uk.co.agena.minerva.model.MarginalDataItemList;
import uk.co.agena.minerva.util.nptgenerator.*;
import java.util.ArrayList;
import java.util.Arrays;
import uk.co.agena.minerva.model.MessagePassingLinkException;
import uk.co.agena.minerva.model.PropagationException;
import uk.co.agena.minerva.model.PropagationTerminatedException;
import uk.co.agena.minerva.model.corebn.CoreBNException;
import uk.co.agena.minerva.model.corebn.CoreBNInconsistentEvidenceException;
import uk.co.agena.minerva.util.Config;
import uk.co.agena.minerva.util.EM.Data;
import uk.co.agena.minerva.util.EM.EMCal;
import uk.co.agena.minerva.util.EM.EMLearningException;
import uk.co.agena.minerva.util.io.CSVWriter;
import uk.co.agena.minerva.util.io.FileHandlingException;
import uk.co.agena.minerva.util.model.NameDescription;
import uk.co.agena.minerva.util.model.SampleDataGenerator;

/**
 * These examples correspond to the ones from AgenaRisk 10 Developer manual.
 *
 * @author Norman Fenton
 * @author Eugene Dementiev
 */
public class DemoLegacy {

    public static void main(String args[]) {
        DemoLegacy ex = new DemoLegacy();
        ex.createSaveLoadSimpleModel();
        ex.editStates();
        ex.nptManipulation();
		ex.learnTablesWithExpertJudgement();
        ex.learnTablesWithExpertJudgementForIndividualNodes();
    }

    public void createSaveLoadSimpleModel() {
        try {
            Model myModel = Model.createEmptyModel();
            // First get the single BN
            ExtendedBN ebn = myModel.getExtendedBNAtIndex(0);
        // Add a new node of type Boolean to this BN;
            // its identifier is “b” ands its name is “B”

            BooleanEN booleanNode = ebn.addBooleanNode("b", "B");
            LabelledEN labelledNode = ebn.addLabelledNode("l", "L");

            labelledNode.addChild(booleanNode);

            myModel.calculate();
            myModel.save("test.cmp");
            Model m = Model.load("test.cmp");

        } catch (Exception e) {
        };
    }

    public void editStates() {

        try {
            Model m = Model.createEmptyModel();
            // First get the single BN
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);

            LabelledEN len = ebn.addLabelledNode("l", "L");
            BooleanEN ben = ebn.addBooleanNode("b", "B");
            ContinuousIntervalEN cien = ebn.addContinuousIntervalNode("ci", "CI");
            IntegerIntervalEN iien = ebn.addIntegerIntervalNode("ii", "II");
            DiscreteRealEN dren = ebn.addDiscreteRealNode("dr", "DR");
            RankedEN ren = ebn.addRankedNode("r", "R");

            //Create DataSet object:
            DataSet lds = new DataSet();

            //Add the set of state names to lds:
            lds.addLabelledDataPoint("Red");
            lds.addLabelledDataPoint("Amber");
            lds.addLabelledDataPoint("Green");

            //Now completely redefine the set of states of len:
            len.createExtendedStates(lds);

            //For the Boolean node:
            DataSet bds = new DataSet();
            bds.addLabelledDataPoint("Yes");
            bds.addLabelledDataPoint("No");
            ben.createExtendedStates(bds);

            //For the Continuous Interval Node:
            DataSet cids = new DataSet();
            cids.addIntervalDataPoint(0.0, 100.0);
            cids.addIntervalDataPoint(100.0, 200.0);
            cids.addIntervalDataPoint(200.0, 300.0);
            cien.createExtendedStates(cids);

            //For the Integer Interval Node:
            DataSet iids = new DataSet();
            iids.addIntervalDataPoint(10, 20);
            iids.addIntervalDataPoint(20, 30);
            iids.addIntervalDataPoint(30, 40);
            iien.createExtendedStates(iids);

            //For the Discrete Real Node:
            DataSet drds = new DataSet();
            drds.addAbsoluteDataPoint(1.0);
            drds.addAbsoluteDataPoint(2.0);
            drds.addAbsoluteDataPoint(3.0);
            dren.createExtendedStates(drds);

            //For the ranked node:
            DataSet rds = new DataSet();
            rds.addLabelledDataPoint("Bad");
            rds.addLabelledDataPoint("OK");
            rds.addLabelledDataPoint("Good");
            ren.createExtendedStates(rds);

        //Finally save the model
            m.save("test.cmp");

        } catch (Exception e) {
        };
    }

    public void nptManipulation() {
        try {
             // Load the model that was created and saved 
            // in the method editStates
            Model m = Model.load("test.cmp");

            // Get the single BN
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);

            //Get the three of the nodes labelled B, L, and R. 
            //Note use of casting as the ebn.getExtendedNodewithName() method returns the ExtendedNode superclass:
            BooleanEN ben = (BooleanEN) ebn.getExtendedNodeWithName("B");
            LabelledEN len = (LabelledEN) ebn.getExtendedNodeWithName("L");
            RankedEN ren = (RankedEN) ebn.getExtendedNodeWithName("R");

            //Next we make nodes B (ben) and R (ren) parents of L (len):
            len.addParent(ben);
            len.addParent(ren);

            //Next redefine the NPTs of nodes B and R to overwrite the default NPTs. 
            //These NPTs are easy because the nodes have no parents. 
            //B has two states so we need to define an array of length 2. 
            // R has three states so we need to define an array of length 3:
            ben.setNPT(new double[]{0.9, 0.1});
            ren.setNPT(new double[]{0.2, 0.3, 0.5});

            //To redefine the NPT for the node L is harder because it has two parents (B and R)
            //To generate this NPT we must first get the list of parents of L:
            List lenParents = ebn.getParentNodes(len);

            //Now we define the NPT of L:
            len.setNPT(new double[][]{{0.7, 0.2, 0.1},
            {0.5, 0.3, 0.2},
            {0.3, 0.4, 0.3},
            {0.1, 0.1, 0.8},
            {0.3, 0.3, 0.4},
            {0.5, 0.4, 0.1}}, lenParents);

               //Save the model:
            m.save("test.cmp");

        } //try
        catch (Exception e) {
        };

    } //nptManipulation

    public void printMarginals(Model m, ExtendedBN ebn, ExtendedNode enode) {

        // Get the Marginals for the node
        MarginalDataItemList mdil
                = m.getMarginalDataStore().getMarginalDataItemListForNode(ebn, enode);

	// There’s only one scenario, so get the first 
        // MarginalDataItem in the list
        MarginalDataItem mdi = mdil.getMarginalDataItemAtIndex(0);

        // Now print out the node name and marginals
        System.out.println(enode.getName().getShortDescription());

        List marginals = mdi.getDataset().getDataPoints();
        for (int i = 0; i < marginals.size(); i++) {
            DataPoint marginal = (DataPoint) marginals.get(i);
            System.out.println(marginal.getLabel()
                    + " = "
                    + marginal.getValue());
        }
        //Print the mean of the distribution
        System.out.println("Mean = " + mdi.getMeanValue());

        //get the variance of the distribution
        System.out.println("Variance = " + mdi.getVarianceValue());

    }

    public void workingWithEvidence() {
        try {
            Model m = Model.load("test.cmp");
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);

            //Calculate the entire model:
            m.calculate();

            //get the node L 
            LabelledEN len = (LabelledEN) ebn.getExtendedNodeWithName("L");
            //print the marginals and statistics for L
            printMarginals(m, ebn, len);
            //get the node CI 
            ContinuousIntervalEN cien = (ContinuousIntervalEN) ebn.getExtendedNodeWithName("CI");
            //print the marginals and statistics for L
            printMarginals(m, ebn, cien);

        } catch (Exception e) {
        };
    } //workingWithEvidence

    public void enteringEvidence() {
        try {
            Model m = Model.load("test.cmp");
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);
            m.calculate();

            //get the node L whose NPT we created in the nptManipulation() method:
            LabelledEN len = (LabelledEN) ebn.getExtendedNodeWithName("L");

            //We are going to enter evidence on one of the parents of the node L. 
            //So, first we need to get one such parent, the Ranked node R:
            RankedEN ren = (RankedEN) ebn.getExtendedNodeWithName("R");

            //All evidence entry is done via “scenarios” 
            //(there is an enterEvidence() method associated with the ExtendedNode class but you should ignore this). 
            //So we need to get the single (default) scenario, which currently has no evidence:
            Scenario s = m.getScenarioAtIndex(0);

            //Now we enter hard evidence on node R (ren):
            s.addHardEvidenceObservation(ebn.getId(), ren.getId(), ren.getExtendedStateAtIndex(2).getId());

            //Now calculate again and print the marginals:
            m.calculate();
            printMarginals(m, ebn, len);

            //Nex we show how to enter evidence for continuous nodes
            //get the continuous node CI (cien):
            ContinuousIntervalEN cien = (ContinuousIntervalEN) ebn.getExtendedNodeWithName("CI");

            //Now enter a “real number” observation, 48.0, using the same scenario s:
            s.addRealObservation(ebn.getId(), cien.getId(), 48.0);

            //Note: For an Integer Interval node you would use the method addIntegerObservation().
            //Calculate the model again and and print the marginals for the node CI (cien):
            m.calculate();
            printMarginals(m, ebn, cien);

            //save the model:
            m.save("test.cmp");

        } catch (Exception e) {
        };
    } //enteringEvidence

    public void testSimpleExpression() {
        try {
            // create a new model and get the single BN:
            Model m = Model.createEmptyModel();
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);

            //Add a new Continuous Interval node to this BN:
            ContinuousIntervalEN a = ebn.addContinuousIntervalNode("a", "A");

            //Redefine the states of node A as just a single state range from 0 to infinity:
            DataSet cids = new DataSet();
            cids.addIntervalDataPoint(0.0, Double.POSITIVE_INFINITY);
            a.createExtendedStates(cids);

            //make this node a simulation node by setting the node's simulation property to true:
            a.setSimulationNode(true);

            //define a Normal distribution as the expression for the NPT of this node:
            List parameters = new ArrayList();
            parameters.add("150"); // mean
            parameters.add("100"); // variance
            ExtendedNodeFunction enf = new ExtendedNodeFunction(Normal.displayName, parameters);
            a.setExpression(enf);

            //Now regenerate the NPT for this node:
            ebn.regenerateNPT(a);

            // setting the number of iterations (the default is 25 if you do not set it explicitly)
            m.setSimulationNoOfIterations(30);

            //Calculate the entire model:
            m.calculate();

            //Use the printMarginals() method defined above to print out the marginals:
            printMarginals(m, ebn, a);

            //save the model so you can inspect it in AgenaRisk:
            m.save("test2.cmp");

        } catch (Exception e) {
        };
    }//testSimpleExpression

    public void testExpressionWithParent() {
        try {
            // create a new model and get the single BN:
            Model m = Model.createEmptyModel();
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);

            //Create three Continuous Interval nodes, A and B, and C:
            ContinuousIntervalEN a = ebn.addContinuousIntervalNode("a", "A");
            ContinuousIntervalEN b = ebn.addContinuousIntervalNode("b", "B");
            ContinuousIntervalEN c = ebn.addContinuousIntervalNode("c", "C");

            //Redefine the states of each node as just a single state range from 0 to infinity
            DataSet cids = new DataSet();
            cids.addIntervalDataPoint(0.0, Double.POSITIVE_INFINITY);
            a.createExtendedStates(cids);
            b.createExtendedStates(cids);
            c.createExtendedStates(cids);

            //Make C the child of both A and B:
            a.addChild(c);
            b.addChild(c);

            //Make all nodes simulation nodes:
            a.setSimulationNode(true);
            b.setSimulationNode(true);
            c.setSimulationNode(true);

            //Define the NPTs of A and B to be Uniform:
            List uniformParameters = new ArrayList();
            uniformParameters.add("0.0"); //lower bound
            uniformParameters.add("10000000000.0"); //upper bound
            ExtendedNodeFunction uniform
                    = new ExtendedNodeFunction(Uniform.displayName, uniformParameters);
            a.setExpression(uniform);
            b.setExpression(uniform);

            //Define the NPT of C as an arithmetic expression of the parents (we use a simple sum):
            List parameters = new ArrayList();
            parameters.add("a + b");
            ExtendedNodeFunction enf = new ExtendedNodeFunction(Arithmetic.displayName, parameters);
            c.setExpression(enf);

            //regenerate the NPTs:
            ebn.regenerateNPT(a);
            ebn.regenerateNPT(b);
            ebn.regenerateNPT(c);

            //Calculate the entire model and print the marginals:
            m.calculate();
            printMarginals(m, ebn, c);

            //Now enter observations for A and B. First we get the single Scenario:
            Scenario s = m.getScenarioAtIndex(0);
            s.addRealObservation(ebn.getId(), a.getId(), 33.0);
            s.addRealObservation(ebn.getId(), b.getId(), 44.0);

            //Calculate again:
            m.calculate();
            printMarginals(m, ebn, c);

            //Save the model:
            m.save("test3.cmp");

        } catch (Exception e) {
        };
    }//testExpressionWithParent

    public void testPartitionedExpression() {
        try {
            // create a new model and get the single BN:
            Model m = Model.createEmptyModel();
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);

            // Create a Labelled node, A, and set up new states for it:
            LabelledEN a = ebn.addLabelledNode("a", "A");
            DataSet lds = new DataSet();
            lds.addLabelledDataPoint("Red");
            lds.addLabelledDataPoint("Amber");
            lds.addLabelledDataPoint("Green");
            a.createExtendedStates(lds);

            //Create a Continuous Interval node, B:
            ContinuousIntervalEN b = ebn.addContinuousIntervalNode("b", "B");

            //Make it a child of A: 
            a.addChild(b);

            //Make it a simulation node:
            b.setSimulationNode(true);

            //Set the new state range to be 0 to infinity:
            DataSet cids = new DataSet();
            cids.addIntervalDataPoint(0.0, Double.POSITIVE_INFINITY);
            b.createExtendedStates(cids);

            //Make A a model node of B:
            List modelNodes = new ArrayList();
            b.setPartitionedExpressionModelNodes(modelNodes);
            modelNodes.add(a);

                //Next we create a partitioned expression for each state of A. 
            //First we need a List to store the expressions we will create:
            List expressions = new ArrayList();
            b.setPartitionedExpressions(expressions);

                //Create the expression Normal(10, 100) for the first state (Red):
            List redParameters = new ArrayList();
            redParameters.add("10");
            redParameters.add("100");
            expressions.add(new ExtendedNodeFunction(Normal.displayName, redParameters));

                //Create the expression Normal(25, 100) for second state (Amber):
            List amberParameters = new ArrayList();
            amberParameters.add("25");
            amberParameters.add("100");
            expressions.add(new ExtendedNodeFunction(Normal.displayName, amberParameters));

            //Create the expression Normal(40, 100) for third state (Green):
            List greenParameters = new ArrayList();
            greenParameters.add("40");
            greenParameters.add("100");
            expressions.add(new ExtendedNodeFunction(Normal.displayName, greenParameters));

            //Now regenerate the NPT for B:
            ebn.regenerateNPT(b);

            //Calculate the entire model and print the marginals:
            m.calculate();
            printMarginals(m, ebn, b);

            //Next we will add observations, so we need to get the single scenario:
            Scenario s = m.getScenarioAtIndex(0);

            //Enter observation Red, calculate and view the marginals:
            s.addHardEvidenceObservation(ebn.getId(), a.getId(), a.getExtendedStateAtIndex(0).getId());
            m.calculate();
            printMarginals(m, ebn, b);

            //Enter observation Amber, calculate and view the marginals:
            s.addHardEvidenceObservation(ebn.getId(), a.getId(), a.getExtendedStateAtIndex(1).getId());
            m.calculate();
            printMarginals(m, ebn, b);

                //Enter observation Green, calculate and view the marginals:
            s.addHardEvidenceObservation(ebn.getId(), a.getId(), a.getExtendedStateAtIndex(2).getId());
            m.calculate();
            printMarginals(m, ebn, b);

            //save the file:
            m.save("test4.cmp");

        } catch (Exception e) {
        };
    }//testPartitionedExpression

    public void testBNOs() {
        try {
            // create an empty model and get the existing BN:

            Model m = Model.createEmptyModel();
            ExtendedBN one = m.getExtendedBNAtIndex(0);

            //Rename the existing BN
            NameDescription name = new NameDescription("One", "First BN");
            one.setName(name);

            //This time we want to create a new BN called “Two”:
            ExtendedBN two = m.addExtendedBN("Two", "Second BN");

                //Use  method populateSimpleExtendedBN to populate both BNs:
            populateSimpleExtendedBN(one);
            populateSimpleExtendedBN(two);

            //get the node A in BN “One” and set it as an output node:
            ExtendedNode source = one.getExtendedNodeWithUniqueIdentifier("a");
            source.setConnectableOutputNode(true);

            //Get the node A in BN “Two” and set it as an input node:
            ExtendedNode target = two.getExtendedNodeWithUniqueIdentifier("a");
            target.setConnectableInputNode(true);

            //Link the two nodes (and, thus, the two BNs)
            m.link(source, target);

            //Add an observation to c in BN "one":
            Scenario s = m.getScenarioAtIndex(0);
            s.addRealObservation(one.getId(), one.getExtendedNodeWithUniqueIdentifier("c").getId(), 30.0);

            //Calculate and print the marginals of a in both BNs:
            m.calculate();
            printMarginals(m, one, one.getExtendedNodeWithUniqueIdentifier("a"));
            printMarginals(m, two, two.getExtendedNodeWithUniqueIdentifier("a"));

            //save the model
            m.save("test5.cmp");

        } catch (Exception e) {
        };
    }//testBNOs

    private void populateSimpleExtendedBN(ExtendedBN ebn) throws Exception {

        // Create two nodes, A and B, and a child C
        ContinuousIntervalEN a = ebn.addContinuousIntervalNode("a", "A");
        ContinuousIntervalEN b = ebn.addContinuousIntervalNode("b", "B");
        ContinuousIntervalEN c = ebn.addContinuousIntervalNode("c", "C");
        a.addChild(c);
        b.addChild(c);

        List parameters = new ArrayList();
        parameters.add("a + b");
        ExtendedNodeFunction enf = new ExtendedNodeFunction(
                Arithmetic.displayName, parameters);
        c.setExpression(enf);

        //For the node c redefine its states
        DataSet cids = new DataSet();
        cids.addIntervalDataPoint(0.0, 10.0);
        cids.addIntervalDataPoint(10.0, 20.0);
        cids.addIntervalDataPoint(20.0, 30.0);
        cids.addIntervalDataPoint(30.0, 40.0);
        c.createExtendedStates(cids);

        // Now regenerate the NPT for C
        ebn.regenerateNPT(a);
        ebn.regenerateNPT(b);
        ebn.regenerateNPT(c);
    } //populateSimpleExtendedBN

    private void printNPTs(ExtendedBN ebn) {
        System.out.println("=======================");
        ((List<ExtendedNode>) ebn.getExtendedNodes())
                .stream()
                .forEach(e -> {
                    System.out.println(e.getConnNodeId() + " " + e + " " + e.getConfidence());
                    try {
                        System.out.println(Arrays.deepToString(e.getNPT()));
                    } catch (ExtendedBNException ex) {
                    }
                });
    }

    /**
     * Demonstrates generating example data file.
     *
     * @throws Exception
     */
    private void generateExampleDataFile() {
        try {
            //Load example model
			Path examplesDirectoryPath = Paths.get(Config.getDirectoryHomeAgenaRisk(), "Model Library", "Advanced", "Learning from Data");
            Model m = Model.load(Paths.get(examplesDirectoryPath.toString(), "Asia.ast").toString());
			//Prepare data path
            
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);
            
            //Number of data rows to be generated
            int numberOfDataSamplesToGenerate = 10;
            
            //File name for example data
            File dataFile = new File("data_example.csv");
            
            //Initialize generator
            SampleDataGenerator generator = new SampleDataGenerator();
            
            //Generate data
            List sampleData = generator.generateDataForEBN(ebn, numberOfDataSamplesToGenerate, true, null);
            
            //Save generated data to a file
            try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(dataFile)), ',', '\0')) {
                writer.writeAll(sampleData);
            } catch (IOException ex) {
                //custom exception handling for problems with saving data file
            }
            
        } catch (FileHandlingException ex) {           
            //custom exception handling for problems with saving data file
        }

    }

    /**
     * Demonstrates learning purely from data.
     *
     * @throws Exception
     */
    private void learnTablesPurelyFromData() {

        try {
            //Load example model
			Path examplesDirectoryPath = Paths.get(Config.getDirectoryHomeAgenaRisk(), "Model Library", "Advanced", "Learning from Data");
            Model m = Model.load(Paths.get(examplesDirectoryPath.toString(), "Asia.ast").toString());
			//Prepare data path
            String dataFileName = Paths.get(examplesDirectoryPath.toString(), "Asia - example dataset.csv").toString();
            
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);
            
            //Print NPTs
            printNPTs(ebn);
            
            //Read in data
            Data data = new Data(dataFileName, "NA");
            
            //If the values in the data file are separated not by a comma, you can provide custom separator
            data = new Data(dataFileName, "NA", ",");
            
            //If the learning process should be logged turn it on
            m.setEMLogging(true);
            
            //Set up EMCal object
            Model.EM_ON = true;
            EMCal emcal = new EMCal(m, ebn, data, "NA", dataFileName, new ArrayList(), false);
            emcal.setMaxIterations(25);
            emcal.threshold = 0.01;
            //emcal.laplacian = 0;
            
            //Start calculations
            emcal.calculateEM();
            
            //Print NPTs
            printNPTs(ebn);
            
            //Save learnt model
            m.save("model_learnt_purely_from_data.cmp");

        } catch (FileHandlingException | IOException | CoreBNException | CoreBNInconsistentEvidenceException | PropagationException | MessagePassingLinkException | PropagationTerminatedException ex) {
            //custom exception handling 
        } catch (InconsistentDataVsModelStatesException ex) {
            //custom exception handling 
        } catch (ExtendedBNException ex) {
            //custom exception handling 
        }
		catch (EMLearningException ex) {
            //custom exception handling 
        }

    }

    /**
     * Demonstrates learning from data and expert judgement (set at the level of
     * ExtendedBN)
     */
    private void learnTablesWithExpertJudgement() {

        try {
			//Load example model
			Path examplesDirectoryPath = Paths.get(Config.getDirectoryHomeAgenaRisk(), "Model Library", "Advanced", "Learning from Data");
            Model m = Model.load(Paths.get(examplesDirectoryPath.toString(), "Asia.ast").toString());
			//Prepare data path
            String dataFileName = Paths.get(examplesDirectoryPath.toString(), "Asia - example dataset.csv").toString();
            
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);
            
            //Print NPTs
            printNPTs(ebn);
            
            //Read in data
            Data data = new Data(dataFileName, "NA");
            
            //If the learning process should be logged turn it on
            m.setEMLogging(true);
            
            //set confidence
            ebn.setConfidence(0.5);
            
            //Set up EMCal object
            Model.EM_ON = true;
            EMCal emcal = new EMCal(m, ebn, data, "NA", dataFileName, Arrays.asList("TBoC"), false);
            emcal.setMaxIterations(25);
            emcal.threshold = 0.01;
            //emcal.laplacian = 0;
            
            //Start calculations
            emcal.calculateEM();
            
            //Print NPTs
            printNPTs(ebn);
            
            //Save learnt model
            m.save("model_learnt_from_data_and_expert_judgement.cmp");

        } catch (FileHandlingException | IOException | CoreBNException | CoreBNInconsistentEvidenceException | PropagationException | MessagePassingLinkException | PropagationTerminatedException ex) {
            //custom exception handling 
        } catch (InconsistentDataVsModelStatesException ex) {
            //custom exception handling 
        } catch (ExtendedBNException ex) {
            //custom exception handling 
        } catch (EMLearningException ex) {
            //custom exception handling 
        }

    }

    /**
     * Demonstrates learning from data and expert judgement (set at the level of
     * individual nodes)
     */
    private void learnTablesWithExpertJudgementForIndividualNodes() {
        try {
            //Load example model
			Path examplesDirectoryPath = Paths.get(Config.getDirectoryHomeAgenaRisk(), "Model Library", "Advanced", "Learning from Data");
            Model m = Model.load(Paths.get(examplesDirectoryPath.toString(), "Asia.ast").toString());
			//Prepare data path
            String dataFileName = Paths.get(examplesDirectoryPath.toString(), "Asia - example dataset.csv").toString();
            
            ExtendedBN ebn = m.getExtendedBNAtIndex(0);
            
            //Print NPTs
            printNPTs(ebn);
            
            //Read in data
            Data data = new Data(dataFileName, "NA");
            
            //If the learning process should be logged turn it on
            m.setEMLogging(true);
            
            //set confidence
            //globally as default for the whole ExtendedBN
            ebn.setConfidence(0.5);
            //only from data
            ebn.getExtendedNodeWithUniqueIdentifier("D").setConfidence(0);
            //knowledge is 3x more important than data
            ebn.getExtendedNodeWithUniqueIdentifier("B").setConfidence(0.75);
            //100% knowledge, do not learn
            ebn.getExtendedNodeWithUniqueIdentifier("L").setConfidence(1);
            //use the default confidence for the whole ExtendedBN but make it a fixed node
            ebn.getExtendedNodeWithUniqueIdentifier("T").setConfidence(-1);
            List<String> fixedNodes = new ArrayList();
            fixedNodes.add("T");
            //try to assign a value form outside possible range
            ebn.getExtendedNodeWithUniqueIdentifier("A").setConfidence(1.3);
            
            //check confidence for individual nodes
            ((List<ExtendedNode>) ebn.getExtendedNodes())
                    .stream()
                    .forEach(e -> System.out.println(e.getConnNodeId() + " " + e + " " + e.getConfidence()));
            
            //Set up EMCal object
            Model.EM_ON = true;
            EMCal emcal = new EMCal(m, ebn, data, "NA", dataFileName, Arrays.asList("TBoC"), false);
            emcal.setMaxIterations(25);
            emcal.threshold = 0.01;
            //emcal.laplacian = 0;
            emcal.setFixedNodes(fixedNodes);
            
            //Start calculations
            emcal.calculateEM();
            
            //Print NPTs
            printNPTs(ebn);
            
            //Save learnt model
            m.save("model_learnt_from_data_and_expert_judgement_individual_nodes.cmp");

        } catch (FileHandlingException | IOException | CoreBNException | CoreBNInconsistentEvidenceException | PropagationException | MessagePassingLinkException | PropagationTerminatedException ex) {
            //custom exception handling 
        } catch (InconsistentDataVsModelStatesException ex) {
            //custom exception handling 
        } catch (ExtendedBNException ex) {
            //custom exception handling 
        } catch (EMLearningException ex) {
            //custom exception handling 
        }

    }

}
