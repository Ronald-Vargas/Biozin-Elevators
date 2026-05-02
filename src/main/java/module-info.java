module com.elevators.biozinelevators {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.elevators.biozinelevators to javafx.fxml;
    exports com.elevators.biozinelevators;
}