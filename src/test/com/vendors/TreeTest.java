package com.vendors;

import com.vendors.app.lib.enums.Rank;
import com.vendors.app.lib.list.List;
import com.vendors.app.models.Vendor;
import com.vendors.app.lib.tree.Tree;
import com.vendors.app.lib.tree.TreeNode;
import com.vendors.app.utils.DataReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

public class TreeTest {
    private Tree tree;

    @BeforeEach
    void setUp() {
        tree = new Tree();
    }

    void initializeTree() {
        initializeTree("vendors");
    }

    void initializeTree(String fileName) {
        File vendorsFile = new File("src\\test\\com\\vendors\\mocks\\" + fileName + ".txt");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(vendorsFile));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        List data = DataReader.getListFromFile(reader);
        tree.insertNodesFromList(data, 0);
    }

    @Test
    void insertNode() {
        // Arrange
        Vendor first = new Vendor(1000, "Joseph", Rank.PLATA, 700000);
        Vendor second = new Vendor(2000, "Maria", Rank.BRONCE, 900000);
        Vendor third = new Vendor(2000, "Luis", Rank.BRONCE, 900000);
        Vendor fourth = new Vendor(2000, "Alex", Rank.ORO, 900000);

        // Act
        tree.insert(first, 0);
        tree.insert(second, first.getCedula());
        tree.insert(third, first.getCedula());
        tree.insert(fourth, first.getCedula());

        // Assert
        TreeNode root = tree.getRoot();
        int childrenCount = root.getNumberChildren();

        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertTrue(root.hasChildren());
        Assertions.assertEquals(3, childrenCount);
    }

    @Test
    void findNodeById() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        long nodeId = 908000;
        TreeNode found = tree.find(nodeId);

        // Assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertNotNull(found);

        long foundNodeId = found.getVendor().getCedula();
        Assertions.assertEquals(nodeId, foundNodeId);
    }

    @Test
    void countTreeLevelsOfRoot() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        int levels = tree.countTreeLevels(tree.getRoot());

        // Assert
        Assertions.assertEquals(7, levels);
    }

    @Test
    void countTreeLevelsOfLocaleRoot() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        TreeNode localeRoot = tree.find(902000);
        int levels = tree.countTreeLevels(localeRoot);

        // Assert
        Assertions.assertEquals(2, levels);
    }
    @Test
    void countTreeLevelsWhenChildrenIsEmpty() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        TreeNode localeRoot = tree.find(556778);
        int levels = tree.countTreeLevels(localeRoot);

        // Assert
        Assertions.assertEquals(0, levels);
    }

    @Test
    void countChildrenByLevelOfRoot() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        int children = tree.countChildrenByLevel(tree.getRoot(), 1);

        // Assert
        Assertions.assertEquals(3, children);
    }

    @Test
    void countChildrenByLevelOfLocaleRoot() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        TreeNode localeRoot = tree.find(556777);
        int children = tree.countChildrenByLevel(localeRoot, 1);

        // Assert
        Assertions.assertEquals(2, children);
    }

    @Test
    void assignRanks() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        tree.assignRanks(tree.getRoot());

        // Assert
        Assertions.assertEquals(Rank.PLATA, tree.getRoot().getVendor().getPreviousRank());
        Assertions.assertEquals(Rank.ORO, tree.getRoot().getVendor().getCurrentRank());
    }

    @Test
    void assignCommissions() {
        // Arrange
        initializeTree("vendors_unordered");
        String[] descriptionsExpected = {
                "20% personal + 1% level 1 + 2% level 2 + 3% level 3",
                "15% personal + 1% level 1 + 2% level 2 + 3% level 3",
                "15% personal"
        };
        double[] commissionsExpected = {
                333900,
                126000,
                90000
        };

        // Act
        tree.assignCommissions(tree.getRoot());

        Vendor person = tree.getRoot().getVendor();
        Vendor secondPerson = tree.find(903000).getVendor();
        Vendor thirdPerson = tree.find(908000).getVendor();

        String[] commissionDescriptions = {
                person.getCommissionDescription(),
                secondPerson.getCommissionDescription(),
                thirdPerson.getCommissionDescription()
        };
        double[] commissions = {
                person.getCommission(),
                secondPerson.getCommission(),
                thirdPerson.getCommission()
        };

        // Assert
        Assertions.assertEquals(descriptionsExpected[0], commissionDescriptions[0]);
        Assertions.assertEquals(commissionsExpected[0], commissions[0]);

        Assertions.assertEquals(descriptionsExpected[1], commissionDescriptions[1]);
        Assertions.assertEquals(commissionsExpected[1], commissions[1]);

        Assertions.assertEquals(descriptionsExpected[2], commissionDescriptions[2]);
        Assertions.assertEquals(commissionsExpected[2], commissions[2]);
    }

    @Test
    void assignCommissionsWithLevelUp() {
        // Arrange
        initializeTree("vendors_unordered");
        String[] descriptionsExpected = {
                "15% level up + 25% personal + 1% level 1 + 2% level 2 + 3% level 3", // from silver to gold
                "15% personal + 1% level 1 + 2% level 2 + 3% level 3", // prev = current (no level up)
                "10% personal" // from gold to cobre (no level up)
        };
        double[] commissionsExpected = {
                423900,
                126000,
                60000
        };

        // Act
        tree.assignRanks(tree.getRoot());
        tree.assignCommissions(tree.getRoot());

        Vendor person = tree.getRoot().getVendor();
        Vendor secondPerson = tree.find(903000).getVendor();
        Vendor thirdPerson = tree.find(908000).getVendor();

        String[] commissionDescriptions = {
                person.getCommissionDescription(),
                secondPerson.getCommissionDescription(),
                thirdPerson.getCommissionDescription()
        };
        double[] commissions = {
                person.getCommission(),
                secondPerson.getCommission(),
                thirdPerson.getCommission()
        };

        // Assert
        Assertions.assertEquals(descriptionsExpected[0], commissionDescriptions[0]);
        Assertions.assertEquals(commissionsExpected[0], commissions[0]);

        Assertions.assertEquals(descriptionsExpected[1], commissionDescriptions[1]);
        Assertions.assertEquals(commissionsExpected[1], commissions[1]);

        Assertions.assertEquals(descriptionsExpected[2], commissionDescriptions[2]);
        Assertions.assertEquals(commissionsExpected[2], commissions[2]);
    }

    @Test
    void childrenSalesOfRoot() {
        // Arrange
        String fileName = "vendors_unordered";
        initializeTree(fileName);

        File vendorsFile = new File("src\\test\\com\\vendors\\mocks\\" + fileName + ".txt");
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(vendorsFile));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        double childrenSales = 0;

        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                String[] attributes = line.trim().split("\t");

                if (attributes.length == 5) {
                    childrenSales += Double.parseDouble(attributes[3]);
                }
            }
            reader.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        // Act
        TreeNode root = tree.getRoot();
        double totalSales = tree.calculateChildrenSales(root);

        // Assert
        Assertions.assertEquals(childrenSales, totalSales);
    }

    @Test
    void childrenSalesOfLocaleRoot() {
        // Arrange
        String fileName = "vendors_unordered";
        initializeTree(fileName);

        File vendorsFile = new File("src\\test\\com\\vendors\\mocks\\" + fileName + ".txt");
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(vendorsFile));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        double childrenSales = 0;

        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                String[] attributes = line.trim().split("\t");

                if (attributes.length == 5 && Long.parseLong(attributes[0]) != 345345) {
                    long parentId = Long.parseLong(attributes[4]);

                    if (parentId == 345345 || parentId == 567543)
                        childrenSales += Double.parseDouble(attributes[3]);
                }
            }
            reader.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        // Act
        TreeNode localeRoot = tree.find(345345);
        double totalSales = tree.calculateChildrenSales(localeRoot);

        // Assert
        Assertions.assertEquals(childrenSales, totalSales);
    }

    @Test
    void calculateChildrenSalesByLevelOfRoot() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        double expectedFirst = 1800000;
        double salesFirstLevel = tree.calculateChildrenSalesByLevel(tree.getRoot(), 1);

        double expectedThird = 2510000;
        double salesThirdLevel = tree.calculateChildrenSalesByLevel(tree.getRoot(), 3);

        // Assert
        Assertions.assertEquals(expectedFirst, salesFirstLevel);
        Assertions.assertEquals(expectedThird, salesThirdLevel);
    }

    @Test
    void calculateChildrenSalesByLevelOfLocaleRoot() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        TreeNode localeRoot = tree.find(909000);

        double expectedFirst = 1200000;
        double salesFirstLevel = tree.calculateChildrenSalesByLevel(localeRoot, 1);

        double expectedFourth = 1000000;
        double salesFourthLevel = tree.calculateChildrenSalesByLevel(localeRoot, 4);

        // Assert
        Assertions.assertEquals(expectedFirst, salesFirstLevel);
        Assertions.assertEquals(expectedFourth, salesFourthLevel);
    }

    @Test
    void existsChildWithRank() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        Rank searchedRank = Rank.ORO;
        TreeNode childWithGold = tree.existsChildWithRank(tree.getRoot(), searchedRank);

        // Assert
        Assertions.assertNotNull(childWithGold);
        Rank rankOfChild = childWithGold.getVendor().getCurrentRank();
        Assertions.assertEquals(rankOfChild, searchedRank);
    }

    @Test
    void notExistsChildWithRank() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        TreeNode localeRoot = tree.find(901000);
        Rank searchedRank = Rank.PLATA;
        TreeNode childWithSilver = tree.existsChildWithRank(localeRoot, searchedRank);

        // Assert
        Assertions.assertNull(childWithSilver);
    }

    @Test
    void calculateLevelCommissionOfRoot() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        double rootCommission = tree.calculateLevelCommission(tree.getRoot());

        // Assert
        Assertions.assertEquals(243900, rootCommission);
    }

    @Test
    void calculateLevelCommissionOfLocaleRoot() {
        // Arrange
        initializeTree("vendors_unordered");

        // Act
        TreeNode localeRoot = tree.find(901000);
        double commission = tree.calculateLevelCommission(localeRoot);

        // Assert
        Assertions.assertEquals(84000, commission);
    }
}
