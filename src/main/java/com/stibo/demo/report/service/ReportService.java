package com.stibo.demo.report.service;

import com.stibo.demo.report.controller.ReportController;
import com.stibo.demo.report.logging.LogTime;
import com.stibo.demo.report.model.*;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

import javax.xml.crypto.Data;

@Service
public class ReportService {

    @LogTime
    public Stream<Stream<String>> report(Datastandard datastandard, String categoryId) {
        // TODO: implement
        // The method takes a data standard object and a category id as argument, and should return a stream of stream of strings.
        // The outer stream is the tabular rows, and the inner streams are the cells in a row. The controller 
        // [endpoint](src/main/java/com/stibo/demo/report/controller/ReportController.java) makes a naive conversion of this to
        // csv.      

        // Find the category we're looking for
        Category category = datastandard.categories().stream().filter(cat -> cat.id().equals(categoryId)).findFirst().orElse(null);
        // Category category = datastandard.categories().stream().filter(cat -> cat.id().equals(categoryId));

        // Skip if no category found
        if (category == null) {
            return Stream.of();
        }
        
        // List all attributes in the category
        // Stream (Title headers)
        // Stream (Root attributes)
        // Stream (Child attributes)
        // Add category attributes, loop through cats where parentID = categoryID, repeat
        return Stream.concat(
            Stream.of(Stream.of("Category Name", "Attribute Name", "Description", "Type", "Groups")), 
            getCategoryAttributes(category, datastandard)
        );
    }

    private Attribute findAttribute(Datastandard datastandard, String attributeId) {
        // The method takes a datastandard object and an attribute id as argument, and should return a single attribute.
        return datastandard.attributes().stream().filter(attr -> attr.id().equals(attributeId)).findFirst().orElse(null);
    }

    private String getMandatory(AttributeLink attrLink) {
        if (attrLink.optional() == null){
            return "";
        }
        return attrLink.optional() == false ? "*" : "";
    }

    private String addSpace(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    private String getAttributeType(Attribute attribute, Datastandard datastandard, int depth) {
        // Get the type of the attribute
        String typeID = attribute.type().id();
        // Find all nested attributes, if any
        boolean hasNested = attribute.attributeLinks() != null  && !attribute.attributeLinks().isEmpty() ;
        String attrLinks = hasNested ?
            "{\n" + attribute.attributeLinks().stream().map((attrLink) -> {
                Attribute attr = findAttribute(datastandard, attrLink.id());
                return ("  " + addSpace(depth) + attr.name() + getMandatory(attrLink) + ": " + getAttributeType(attr, datastandard, depth + 1) + "\n");
            }).reduce("", (a, b) -> a + b) + addSpace(depth) + "}" : "";
        // construct nested values string for output
        //String nestedVals = hasNested ? "{" + attrLinks + "}" : "";

        return typeID + attrLinks + (attribute.type().multiValue() ? "[]" : "");
    }

    private Stream<Stream<String>> getChildCategoryAttributes(Category category, Datastandard datastandard) {
        return datastandard.categories().stream().filter(cat -> cat.parentId() != null && cat.parentId().equals(category.id())).flatMap(child -> getCategoryAttributes(child, datastandard));
    }

    private Stream<Stream<String>> getParentCategoryAttributes(Category category, Datastandard datastandard) {   
        if (category.parentId() == null) {
            return Stream.of();
        }    
        Category parentCat = datastandard.categories().stream().filter(cat -> cat.id().equals(category.parentId())).findFirst().orElse(null);
        return getCategoryAttributes(parentCat, datastandard);
    }

    private Stream<Stream<String>> getCategoryAttributes(Category category, Datastandard datastandard) {
        var categoryAttributes = category.attributeLinks().stream().map((attrLink) -> {
            Attribute attr = findAttribute(datastandard, attrLink.id());


            // Stream (Category Name, Attribute Name (* = mandatory), Description, Type({}[] optional), Groups)
            return Stream.of(
                category.name(),
                attr.name() + getMandatory(attrLink),
                attr.description(),
                getAttributeType(attr, datastandard, 0),
                attr.groupIds().isEmpty() ? "" : String.join("\n", attr.groupIds())
            );
        });

        // Recursilvely loop down the selected Category's hierarchy, displaying all attributes
        return Stream.concat(
            // getParentCategoryAttributes(category, datastandard),
            categoryAttributes,
            getChildCategoryAttributes(category, datastandard)
            //datastandard.categories().stream().filter(cat -> cat.parentId() != null && cat.parentId().equals(categoryId)).flatMap(child -> getCategoryAttributes(child, datastandard))
        );
    }
}
