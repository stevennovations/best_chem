/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package purchases;

import best_chem.AbstractController;
import dbquerries.InventoryQuery;
import dbquerries.PurchasesQuery;
import dbquerries.SupplierQuery;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.InventoryModel;
import models.PurchaseItemModel;
import models.PurchasesModel;
import models.SupplierModel;
import models.UserModel;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * FXML Controller class
 *
 * @author Steven
 */
public class PurchaseDocumentController extends AbstractController implements Initializable {
    
    @FXML
    private TextField addressfld;

    @FXML
    private Button editbtn;

    @FXML
    private TextField compfld;

    @FXML
    private TextField tinfld;

    @FXML
    private TextField contactbx;

    @FXML
    private DatePicker drdatefld;

    @FXML
    private Button cancelbtn;
    
    @FXML
    private Label itemNum;

    @FXML
    private TableView<PurchaseItemModel> itemlist;

    @FXML
    private TextField bsnstylefld;

    @FXML
    private Button addbtn;

    @FXML
    private TextField totalfld;

    @FXML
    private Button deletebtn;

    @FXML
    private Button resetbtn;

    @FXML
    private TextField pymttermfld;

    @FXML
    private DatePicker datefld;

    @FXML
    private TextField idlfd;

    @FXML
    private Button pendingbtn;
    
    @FXML
    private Button printbtn;
    
    @FXML
    private Button pgrbtn;

    @FXML
    private TextField poidfld;
    
    private SupplierModel supplier;
    
    private final SupplierQuery supq = new SupplierQuery();
    
    private final PurchasesQuery pq = new PurchasesQuery();
    private final InventoryQuery iq = new InventoryQuery();
    
    private ArrayList<PurchaseItemModel> items;
    private ArrayList<PurchaseItemModel> deletedList = new ArrayList();
    
    private PurchasesModel pm;
    
    private boolean isEdit;

    @FXML
    public void AddItem(ActionEvent event) throws IOException, SQLException {
        
        if(this.items.size() != 16){
            FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/purchases/POItems.fxml"));
            Parent root = (Parent) fxmlloader.load();

            POItemsController poic = fxmlloader.<POItemsController>getController();
            poic.initData(super.getGlobalUser(), super.getType());

            Scene scene = new Scene(root);
            Stage stage = (Stage) addbtn.getScene().getWindow();
            Stage substage = new Stage();
            substage.setScene(scene);
            substage.setTitle("Add Item");
            substage.initModality(Modality.WINDOW_MODAL);
            substage.initOwner(stage);
            substage.showAndWait();

            if(!poic.IsCancelled()){
                InventoryModel item = poic.getItem();
                PurchaseItemModel poitem = new PurchaseItemModel(item.getIdinventory());
                poitem.setDesc(item.getDescription());
                poitem.setQty(poic.getQty());
                poitem.setSku(item.getSku());
                poitem.setUom(item.getUom());
                poitem.setUprice(item.getPoprice());

                double amount = item.getPoprice() * poic.getQty();
                System.out.println(amount);
                double vatVal = 1 + (12.0 / 100.0);
                System.out.println(vatVal);
                double vat = amount * vatVal;
                vat = Math.round(vat * 100.0)/100.0;

                poitem.setAmount(amount);
                poitem.setVat(vat);
                System.out.println(amount);
                System.out.println(vat);

                if(!this.items.contains(poitem)){
                    items.add(poitem);
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("You have already selected this item.");

                    alert.showAndWait();
                }

                this.RefreshItems();
                this.computeTotal();

            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("You have exceeded 16 items.");

            alert.showAndWait();
        }
    }
    
    public void computeTotal(){
        
        double total = 0.0;
        
        for(PurchaseItemModel model : this.items){
            total += model.getAmount();
        }
        
        NumberFormat nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_EVEN);
        
        this.totalfld.setText(nf.format(total));
    }

    @FXML
    void EditItem(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Edit Item Quantity");
        dialog.setHeaderText("Item: " + this.itemlist.getSelectionModel().getSelectedItem().getSku() + "-" 
                + this.itemlist.getSelectionModel().getSelectedItem().getDesc() + "\n" 
                + "Current Quantity: " + this.itemlist.getSelectionModel().getSelectedItem().getQty());

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            
            System.out.println(this.itemlist.getSelectionModel().getSelectedIndex());
            System.out.println(this.itemlist.getSelectionModel().getFocusedIndex());
            
            double amount = this.itemlist.getSelectionModel().getSelectedItem().getUprice() * Integer.valueOf(result.get());
            
            double vatVal = 1 + (12.0 / 100.0);
            double vat = amount * vatVal;
            vat = Math.round(vat * 100.0)/100.0;
            System.out.println(vat);
          
            this.items.get(this.itemlist.getSelectionModel().getSelectedIndex()).setQty(Integer.valueOf(result.get()));
            this.items.get(this.itemlist.getSelectionModel().getSelectedIndex()).setAmount(amount);
            this.items.get(this.itemlist.getSelectionModel().getSelectedIndex()).setVat(vat);
            this.itemlist.getItems().clear();
            this.RefreshItems();
            this.computeTotal();
        }
    }

    @FXML
    void DeleteItem(ActionEvent event) {
        if(isEdit){
            if(!this.items.isEmpty()){
                this.deletedList.add(this.items.remove(this.itemlist.getSelectionModel().getFocusedIndex()));
                this.computeTotal();
                this.RefreshItems();
            }
        }else{
            if(!this.items.isEmpty()){
                this.items.remove(this.itemlist.getSelectionModel().getSelectedItem());
                this.computeTotal();
                this.RefreshItems();
            }
        }
    }

    @FXML
    void ResetItems(ActionEvent event) {
        if(isEdit){
            if(!this.items.isEmpty()){
                for(PurchaseItemModel mod : this.items){
                    this.deletedList.add(mod);
                }
                this.items.clear();
                this.totalfld.setText("0.0");
                this.RefreshItems();
            }
            
        }else{
            if(!this.items.isEmpty()){
                this.items.clear();
                this.totalfld.setText("0.0");
                this.RefreshItems();
            }
        }
    }

    @FXML
    public void saveHandler(ActionEvent event) throws SQLException {
        
        PurchasesModel pomod = new PurchasesModel();
        
        pomod.setSup_id(this.supplier.getSupid());
        pomod.setSupcname(this.contactbx.getText());
        pomod.setPo_dte(Date.valueOf(this.datefld.getValue().toString()));
        pomod.setPo_dr_dte(Date.valueOf(this.drdatefld.getValue().toString()));
        pomod.setCbyid(super.getGlobalUser().getId());
        
        pomod.setPurchases(this.items);
        
        if(isEdit){
            pomod.setIdpurchases(this.pm.getIdpurchases());
            ArrayList<PurchaseItemModel> models = new ArrayList();
            for(PurchaseItemModel mod: this.items){
                if(mod.getIdpurchaseitem() == 0){
                    models.add(mod);
                }
            }
            
            pq.editPurchases(pomod, super.getType());
            if(!this.items.isEmpty()){
                pq.editPurchaseItems(this.items.iterator(), super.getType());
            }
            if(!this.deletedList.isEmpty()){
                pq.deletePurchaseItems(this.deletedList.iterator(), super.getType());
            }
            if(!models.isEmpty()){
                pq.addPurchaseItems(models.iterator(), this.pm.getIdpurchases(), super.getType());
            }
        }else{
            pq.addPurchases(pomod, super.getType());
        }
        
        
        Stage stage = (Stage) cancelbtn.getScene().getWindow();
        stage.close();

    }
    
    public void RefreshItems(){
        String[] arr = {"sku", "desc", "qty1", "uom", "uprice1", "amount1", "vat1"};
        ObservableList<PurchaseItemModel> data
                = FXCollections.observableArrayList();
        
        for(int i = 0; i < this.items.size(); i++){
            this.items.get(i).setAmount1();
            this.items.get(i).setUprice1();
            this.items.get(i).setVat1();
            this.items.get(i).setQty1();
            data.add(items.get(i));
        }
        
        ObservableList<TableColumn<PurchaseItemModel, ?>> olist = (ObservableList<TableColumn<PurchaseItemModel, ?>>) itemlist.getColumns();
        
        for (int i = 0; i < olist.size(); i++) {
            olist.get(i).setCellValueFactory(
                    new PropertyValueFactory<>(arr[i])
            );
            
            if(super.getType() == 2){
                if(i == 0){
                    olist.get(i).setText("PM Code#");
                }
                else if(i == 1){
                    olist.get(i).setText("PM Description#");
                }
                
            }
            
            if(i == 2 || i == 4 || i == 5 || i == 6){
                olist.get(i).setStyle("-fx-alignment: CENTER-RIGHT;");
            }
            else if(i == 3){
                olist.get(i).setStyle("-fx-alignment: CENTER;");
            }
            else{
                olist.get(i).setStyle("-fx-alignment: CENTER-LEFT;");
            }
        }
        
        this.itemNum.setText(String.valueOf(this.items.size()));
        this.itemlist.setItems(data);
    }

    @FXML
    void cancelHandler(ActionEvent event) {
        Stage stage = (Stage) cancelbtn.getScene().getWindow();
        stage.close();
    }
    
    public void setSupplier(SupplierModel supplier){
        this.supplier = supplier;
    }
    
    public void AddMode() throws SQLException{
        
        this.poidfld.setDisable(true);
        this.idlfd.setText(String.valueOf(this.supplier.getSupid()));
        this.idlfd.setEditable(false);
        this.compfld.setText(this.supplier.getSupname());
        this.compfld.setEditable(false);
        this.bsnstylefld.setText(this.supplier.getSupbustyp());
        this.bsnstylefld.setEditable(false);
        this.tinfld.setText(this.supplier.getSuptin());
        this.tinfld.setEditable(false);
        this.datefld.setValue(LocalDate.now());
        this.pymttermfld.setText(this.supplier.getSuppymttrm());
        this.pymttermfld.setEditable(false);
        this.addressfld.setText(this.supplier.getSupaddress());
        this.addressfld.setEditable(false);
        this.pgrbtn.setDisable(true);
    }
    
    public void EditMode(PurchasesModel purchase) throws SQLException{
        
        this.pm = purchase;
        this.isEdit = true;
        this.poidfld.setText(String.valueOf(purchase.getIdpurchases()));
        this.poidfld.setEditable(false);
        this.idlfd.setText(String.valueOf(this.supplier.getSupid()));
        this.idlfd.setEditable(false);
        this.compfld.setText(this.supplier.getSupname());
        this.compfld.setEditable(false);
        this.bsnstylefld.setText(this.supplier.getSupbustyp());
        this.bsnstylefld.setEditable(false);
        this.tinfld.setText(this.supplier.getSuptin());
        this.tinfld.setEditable(false);
        this.datefld.setValue(LocalDate.parse(purchase.getPo_dte().toString()));
        this.pymttermfld.setText(this.supplier.getSuppymttrm());
        this.pymttermfld.setEditable(false);
        this.addressfld.setText(this.supplier.getSupaddress());
        this.addressfld.setEditable(false);
        this.drdatefld.setValue(LocalDate.parse(purchase.getPo_dr_dte().toString()));
        this.contactbx.setText(purchase.getSupcname());
        
        this.pgrbtn.setDisable(true);
        this.printbtn.setDisable(true);
        
        Iterator iterate = pq.getPurchaseOrderItems(purchase.getIdpurchases(), super.getType());
        
        while(iterate.hasNext()){
            HashMap map = (HashMap) iterate.next();
            
            PurchaseItemModel pim = new PurchaseItemModel(Integer.valueOf(map.get("idinventory").toString()));
            
            pim.setIdpurchaseitem(Integer.parseInt(map.get("idpurchaseitems").toString()));
            pim.setSku(map.get("sku").toString());
            pim.setDesc(map.get("skudesc").toString());
            pim.setQty(Integer.parseInt(map.get("po_qty").toString()));
            pim.setUom(map.get("skuom").toString());
            pim.setUprice(Double.parseDouble(map.get("unitprice").toString()));
            pim.setAmount(Double.parseDouble(map.get("amount").toString()));
            pim.setVat(Double.parseDouble(map.get("vat_amount").toString()));
            
            this.items.add(pim);
            
        }
        
        this.pgrbtn.setDisable(true);
        this.RefreshItems();
        this.computeTotal();
        
    }
    
    public void ViewMode(PurchasesModel purchase) throws SQLException{
        
        this.pm = purchase;
        this.poidfld.setText(String.valueOf(purchase.getIdpurchases()));
        this.poidfld.setEditable(false);
        this.idlfd.setText(String.valueOf(this.supplier.getSupid()));
        this.idlfd.setEditable(false);
        this.compfld.setText(this.supplier.getSupname());
        this.compfld.setEditable(false);
        this.bsnstylefld.setText(this.supplier.getSupbustyp());
        this.bsnstylefld.setEditable(false);
        this.tinfld.setText(this.supplier.getSuptin());
        this.tinfld.setEditable(false);
        this.datefld.setValue(LocalDate.parse(purchase.getPo_dte().toString()));
        this.pymttermfld.setText(this.supplier.getSuppymttrm());
        this.pymttermfld.setEditable(false);
        this.addressfld.setText(this.supplier.getSupaddress());
        this.addressfld.setEditable(false);
        this.drdatefld.setValue(LocalDate.parse(purchase.getPo_dr_dte().toString()));
        this.contactbx.setText(purchase.getSupcname());
        
        Iterator iterate = pq.getPurchaseOrderItems(purchase.getIdpurchases(), super.getType());
        
        while(iterate.hasNext()){
            HashMap map = (HashMap) iterate.next();
            
            PurchaseItemModel pim = new PurchaseItemModel(Integer.valueOf(map.get("idinventory").toString()));
            
            pim.setIdpurchaseitem(Integer.parseInt(map.get("idpurchaseitems").toString()));
            pim.setSku(map.get("sku").toString());
            pim.setDesc(map.get("skudesc").toString());
            pim.setQty(Integer.parseInt(map.get("po_qty").toString()));
            pim.setUom(map.get("skuom").toString());
            pim.setUprice(Double.parseDouble(map.get("unitprice").toString()));
            pim.setAmount(Double.parseDouble(map.get("amount").toString()));
            pim.setVat(Double.parseDouble(map.get("vat_amount").toString()));
            
            this.items.add(pim);
            
        }
        
        this.pendingbtn.setDisable(true);
        this.addbtn.setDisable(true);
        this.deletebtn.setDisable(true);
        this.editbtn.setDisable(true);
        this.resetbtn.setDisable(true);
        
        if(this.pm.getPgistat().equals("Y")){
            this.pgrbtn.setDisable(true);
        }
        this.RefreshItems();
        this.computeTotal();
        
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        this.items = new ArrayList();
    }    

    @Override
    public void initData(UserModel user, int type) {
        super.setGlobalUser(user);
        super.setType(type);
        
        ObservableList<TableColumn<PurchaseItemModel, ?>> olist = (ObservableList<TableColumn<PurchaseItemModel, ?>>) itemlist.getColumns();
        
        for (int i = 0; i < olist.size(); i++) {
            
            if(super.getType() == 2){
                if(i == 0){
                    olist.get(i).setText("PM Code#");
                }
                else if(i == 1){
                    olist.get(i).setText("PM Description");
                }
                
            }
        }
    }

    @FXML
    void PostToInventory(ActionEvent event) throws SQLException, IOException {
        
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/purchases/PGRDocument.fxml"));
        Parent root = (Parent) fxmlloader.load();

        PGRDocumentController poic = fxmlloader.<PGRDocumentController>getController();
        poic.initData(super.getGlobalUser(), super.getType());
        poic.setPOItems(this.items.iterator(), this.pm.getIdpurchases(), this.supplier);

        Scene scene = new Scene(root);
        Stage stage = (Stage) this.pgrbtn.getScene().getWindow();
        Stage substage = new Stage();
        substage.setScene(scene);
        substage.setTitle("Post Good Receipt");
        substage.initModality(Modality.WINDOW_MODAL);
        substage.initOwner(stage);
        substage.showAndWait();
        
        Stage stage2 = (Stage) cancelbtn.getScene().getWindow();
        stage2.close();
    }

    @FXML
    void export(ActionEvent event) throws FileNotFoundException, IOException, SQLException {
        
        NumberFormat nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_EVEN);
        
        FileInputStream file = new FileInputStream("C:\\res\\poform.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFRow sheetrow = null;
        Cell cell = null;
        int rownum = 0;
        int cellnum = 0;
        
        /**
         * UPDATE HEADERS
         * 
         *          Row     Col
         *  DATE     8       2
         *  NO:      6       4
         *  SUPPLIER 8       0
         *  Attention9       0
         *  Terms    10      0
         *  DR date  10      2
         *  Items
         *          14-36
         *  
         */
        
        //Date
        rownum = 8;
        cellnum = 4;
        String str3 = "DATE: ";
        str3 += this.datefld.getValue().toString();
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, str3);
        
        //No:
        rownum = 6;
        cellnum = 4;
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, this.poidfld.getText());
        
        //Supplier
        rownum = 8;
        cellnum = 0;
        String str = "SUPPLIER: " + this.supplier.getSupname();
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, str);
        
        //Attention
        rownum = 9;
        cellnum = 0;
        String str2 = "ATTENTION: " + this.contactbx.getText();
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, str2);
        
        //Date
        rownum = 10;
        cellnum = 0;
        String trm = "TERM: " + this.supplier.getSuppymttrm();
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, trm);
        
        //DR Date
        rownum = 10;
        cellnum = 2;
        
        String str4 = ""; 
        for(int i = 0; i < 6; i++){
            str4 += "\u00a0 ";
        }
        str4 += "DELIVERY DATE: ";
        for(int i = 0; i < 12; i++){
            str4 += "\u00a0 ";
        }
        str4 += this.drdatefld.getValue().toString();
        
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, str4);
        
        //Items
        int start = 14;
        XSSFCellStyle txtstyle = workbook.createCellStyle();
        XSSFFont txtfont = workbook.createFont();
        txtfont.setFontName("Calibri");
        txtfont.setFontHeightInPoints((short)10);
        txtstyle.setFont(txtfont);
        txtstyle.setBorderRight(BorderStyle.THIN);
        txtstyle.setBorderLeft(BorderStyle.THIN);
        
        XSSFCellStyle txtstyle2 = workbook.createCellStyle();
        XSSFFont txtfont2 = workbook.createFont();
        txtfont2.setFontName("Calibri");
        txtfont2.setFontHeightInPoints((short)10);
        txtstyle2.setFont(txtfont);
        txtstyle2.setAlignment(HorizontalAlignment.RIGHT);
        txtstyle2.setBorderRight(BorderStyle.THIN);
        txtstyle2.setBorderLeft(BorderStyle.THIN);
        
        XSSFCellStyle txtstyle3 = workbook.createCellStyle();
        XSSFFont txtfont3 = workbook.createFont();
        txtfont3.setFontName("Calibri");
        txtfont3.setFontHeightInPoints((short)10);
        txtstyle3.setFont(txtfont);
        txtstyle3.setAlignment(HorizontalAlignment.CENTER);
        txtstyle3.setBorderRight(BorderStyle.THIN);
        txtstyle3.setBorderLeft(BorderStyle.THIN);
        
        
        for(int x = 0; x < this.items.size(); x++){
            
            rownum = start;
            cellnum = 0;
            this.createCell(sheetrow, sheet, cell, rownum, cellnum, this.items.get(x).getDesc(), txtstyle);
            
            rownum = start;
            cellnum = 1;
            this.createCell(sheetrow, sheet, cell, rownum, cellnum, this.items.get(x).getUom(), txtstyle3);
            
            rownum = start;
            cellnum = 2;
            this.createCell(sheetrow, sheet, cell, rownum, cellnum, this.items.get(x).getQty1(), txtstyle2);
            
            rownum = start;
            cellnum = 3;
            this.createCell(sheetrow, sheet, cell, rownum, cellnum, nf.format(this.items.get(x).getUprice()), txtstyle2);
            
            rownum = start;
            cellnum = 4;
            this.createCell(sheetrow, sheet, cell, rownum, cellnum, nf.format(this.items.get(x).getAmount()), txtstyle2);
            
            start++;
        }
        
        rownum = start;
        cellnum = 0;
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, "********NOTHING FOLLOWS********", txtstyle);
        
        //Total Sales
        rownum = 38;
        cellnum = 4;
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, this.totalfld.getText(), txtstyle2);
        
        //Total Sales
        rownum = 40;
        cellnum = 4;
        this.createCell(sheetrow, sheet, cell, rownum, cellnum, this.totalfld.getText(), txtstyle2);
        
        file.close();
        
        String currentUsersHomeDir = System.getProperty("user.home");
        File dir = new File(currentUsersHomeDir + "\\Documents\\Exports");
        if(!dir.exists()){
            System.out.println("Directory Created");
            dir.mkdir();
        }
        
        String filename = "[PO]" + this.supplier.getSupname() + "-(" + this.pm.getIdpurchases() + ").xlsx";
        
        File file2 = new File(dir.getAbsolutePath()+ "\\" + filename);
        if(!file2.exists()){
            file2.createNewFile();
        }
        
        FileOutputStream outFile =new FileOutputStream(file2);
        workbook.write(outFile);
        outFile.close();
        
        pq.printed(Integer.valueOf(this.poidfld.getText()), super.getType());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Please check the export in My Documents/Export");

        alert.showAndWait();
        
        Stage stage = (Stage) this.printbtn.getScene().getWindow();
        stage.close();
    }
    
    
    private void createCell(XSSFRow sheetrow, XSSFSheet sheet, Cell cell, int rownum, int cellnum, String value){
        sheetrow = sheet.getRow(rownum);
        sheetrow = this.checkRow(sheetrow, sheet, rownum);
        
        cell = sheetrow.getCell(cellnum);
        cell = this.checkCell(cell, sheetrow, cellnum);
        cell.setCellValue(value);
    }
    
    private void createCell(XSSFRow sheetrow, XSSFSheet sheet, Cell cell, int rownum, int cellnum, String value, XSSFCellStyle txtstyle){
        sheetrow = sheet.getRow(rownum);
        sheetrow = this.checkRow(sheetrow, sheet, rownum);
        
        cell = sheetrow.getCell(cellnum);
        cell = this.checkCell(cell, sheetrow, cellnum);
        cell.setCellValue(value);
        cell.setCellStyle(txtstyle);
    }
    
    private XSSFRow checkRow(XSSFRow sheetrow, XSSFSheet sheet, int rownum){
        if(sheetrow == null){
            sheetrow = sheet.createRow(rownum);
        }
        
        return sheetrow;
    }
    
    private Cell checkCell(Cell cell, XSSFRow sheetrow, int cellnum){
        if(cell == null){
            cell = sheetrow.createCell(cellnum);
        }
        
        return cell;
    }
}
