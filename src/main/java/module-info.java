module com.psychologie {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires com.google.gson;
    requires java.sql;
    requires jbcrypt;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.psychologie to javafx.fxml;
    opens com.psychologie.controller to javafx.fxml;
    opens com.psychologie.controller.admin to javafx.fxml;
    opens com.psychologie.controller.patient to javafx.fxml;
    opens com.psychologie.controller.psychologue to javafx.fxml;
    opens com.psychologie.util to javafx.fxml;
    opens com.psychologie.model to javafx.base, com.google.gson;

    exports com.psychologie;
    exports com.psychologie.model;
    exports com.psychologie.controller;
    exports com.psychologie.controller.admin;
    exports com.psychologie.controller.patient;
    exports com.psychologie.controller.psychologue;
    exports com.psychologie.util;
}
