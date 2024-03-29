/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventory;

import best_chem.AbstractController;
import models.InventoryModel;
import dbquerries.InventoryQuery;
import dbquerries.UtilitiesQuery;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.UserModel;

/**
 * FXML Controller class
 *
 * @author Steven
 */
public class InventoryItemController extends AbstractController implements Initializable {
    
    private ArrayList<String> uoms = new ArrayList();
    private ArrayList<String> whs = new ArrayList();
    
    private boolean isEdit = false;
    
    
    @FXML
    private TextField cslfld;

    @FXML
    private ComboBox<String> wrhsfld;

    @FXML
    private TextField skufld;

    @FXML
    private TextField sdescfld;

    @FXML
    private TextField sohfld;
    
    @FXML
    private TextField unitsfld;

    @FXML
    private Button cancelbtn;

    @FXML
    private ComboBox<String> uomfld;

    @FXML
    private Button savebtn;
    
    @FXML
    private Label desclabel;

    @FXML
    private Label skulabel;
    
    @FXML
    private TextField typefld;

    //Queries
    private final UtilitiesQuery uq = new UtilitiesQuery();
    private InventoryModel model;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        Iterator rs = null;
        Iterator rs2 = null;
        try {
            rs = uq.getUOM();
            rs2 = uq.getWHS();
        } catch (SQLException ex) {
            Logger.getLogger(InventoryItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        while(rs.hasNext()){
            uoms.add((String)rs.next());
        }
        while(rs2.hasNext()){
            whs.add((String)rs2.next());
        }
        
        this.uomfld.getItems().addAll(uoms);
        this.wrhsfld.getItems().addAll(whs);
        this.uomfld.getSelectionModel().selectFirst();
        this.wrhsfld.getSelectionModel().selectFirst();
        this.initData(null, super.getType());
    }    
    
    
    public void AddMode(){
        
    }
    
    public void EditMode(InventoryModel model){
        this.isEdit = true;
        
        this.model = model;
        
        this.skufld.setText(model.getSku());
        this.sdescfld.setText(model.getDescription());
        this.uomfld.getSelectionModel().select(model.getUom());
        this.wrhsfld.getSelectionModel().select(model.getWh());
        this.sohfld.setText(String.valueOf(model.getSoh()));
        this.sohfld.setDisable(true);
        this.cslfld.setText(String.valueOf(model.getCsl()));
        this.unitsfld.setText(String.valueOf(model.getUnits()));
        this.typefld.setText(model.getInvent_type());
        
        this.savebtn.setText("Save Item");
    }
    
    @FXML
    public void saveInventory(ActionEvent event) throws SQLException {
        InventoryModel inventory;
        
        inventory = new InventoryModel(null);
        inventory.setSku(this.skufld.getText());
        inventory.setDescription(this.sdescfld.getText());
        inventory.setUom(this.uomfld.getSelectionModel().getSelectedItem());
        inventory.setWh(this.wrhsfld.getSelectionModel().getSelectedItem());
        inventory.setCsl(Integer.parseInt(this.cslfld.getText()));
        inventory.setUnits(Integer.parseInt(this.unitsfld.getText()));
        inventory.setInvent_type(this.typefld.getText());
        
        
        InventoryQuery iq = new InventoryQuery();
        
        if(this.isEdit){
            inventory.setIdinventory(this.model.getIdinventory());
            iq.editInventory(inventory, super.getType());
        }
        else{
            inventory.setSoh(Integer.parseInt(this.sohfld.getText()));
            if(iq.checkInventory(inventory, super.getType())){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("This Item already Exists");

                alert.showAndWait();
            }else{
                iq.addInventory(inventory, super.getType());
                
                Stage stage = (Stage) cancelbtn.getScene().getWindow();
                stage.close();
            }
            
        }
        
        
    }
    
    @FXML
    public void cancelHandler(ActionEvent event) {
        Stage stage = (Stage) cancelbtn.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initData(UserModel user, int type) {
        super.setType(type);
        super.setGlobalUser(user);
        
        if(type == 2){
            this.desclabel.setText("PM Description");
            this.skulabel.setText("PM Code#");
        }
    }
    
}
