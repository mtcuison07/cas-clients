package org.guanzon.cas.clients.account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.appdriver.iface.GTransaction;
import org.guanzon.cas.model.clients.Model_Client_Master;
import org.guanzon.cas.model.clients.ar.Model_AR_Client_Ledger;
import org.guanzon.cas.model.clients.ar.Model_AR_Client_Master;
import org.guanzon.cas.models.Model_Account_Accreditation;
import org.guanzon.cas.parameters.Category;
import org.guanzon.cas.validators.ValidatorFactory;
import static org.guanzon.cas.validators.ValidatorFactory.ClientTypes.COMPANY;
import static org.guanzon.cas.validators.ValidatorFactory.ClientTypes.INDIVIDUAL;
import static org.guanzon.cas.validators.ValidatorFactory.ClientTypes.PARAMETER;
import static org.guanzon.cas.validators.ValidatorFactory.ClientTypes.STANDARD;
import org.guanzon.cas.validators.ValidatorInterface;
import org.json.simple.JSONObject;

/**
 *
 * @author Maynard
 */
public class Account_Accreditation implements GTransaction {

    GRider poGRider;
    boolean pbWthParent;
    int pnEditMode;
    String psTranStatus;
    String psTransNox;
    String psAccountType = "0";
    ArrayList<Model_Account_Accreditation> poModel;
    JSONObject poJSON;
    public String getTransNox(){
        return psTransNox;
    }
    public Account_Accreditation(GRider foGRider, boolean fbWthParent) {
        poGRider = foGRider;
        pbWthParent = fbWthParent;
        pnEditMode = EditMode.UNKNOWN;
    }
    public void setAccountType(String type){
        this.psAccountType = type;
    }
    @Override
    public JSONObject newTransaction() {
        poJSON = new JSONObject();
        try{
            poModel = new ArrayList<>();
            
            addDetail();
                
                
            poJSON.put("result", "success");
            poJSON.put("message", "initialized new record.");
            pnEditMode = EditMode.ADDNEW;
               
        }catch(NullPointerException e){
            
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        
        return poJSON;
    }

    @Override
    public JSONObject openTransaction(String fsValue) {
    
        pnEditMode = EditMode.READY;
        poJSON = new JSONObject();
        
        poJSON = OpenAccount(fsValue);
        return poJSON;
    }

    @Override
    public JSONObject updateTransaction() {
        
        poJSON = new JSONObject();
        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid edit mode.");
            return poJSON;
        }
        pnEditMode = EditMode.UPDATE;
        poJSON.put("result", "success");
        poJSON.put("message", "Update mode success.");
        return poJSON;
    }

    @Override
    public JSONObject saveTransaction() {
        if (!pbWthParent) {
            poGRider.beginTrans();
        }

        poJSON = saveRecord("");

        if ("success".equals((String)poJSON.get("result"))) {
            if (!pbWthParent) {
                poGRider.commitTrans();
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }
        }

        return poJSON;
    }

    @Override
    public JSONObject deleteTransaction(String string) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public JSONObject closeTransaction(String fsValue) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY || getEditMode() == EditMode.UPDATE) {
            
            poJSON = saveRecord("Close");
//            poJSON = poModel.setTranStatus(TransactionStatus.STATE_CLOSED);
//
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject postTransaction(String fsValue) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            poJSON = saveRecord("Post");
//            poJSON = poModel.get(0).setTranStatus(TransactionStatus.STATE_POSTED);
//
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject voidTransaction(String string) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            
            poJSON = saveRecord("Void");
//            poJSON = poModel.setTranStatus(TransactionStatus.STATE_VOID);
//
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }

    @Override
    public JSONObject cancelTransaction(String fsTransNox) {
        poJSON = new JSONObject();

        if (getEditMode() == EditMode.READY
                || getEditMode() == EditMode.UPDATE) {
            
            poJSON = saveRecord("Cancel");
//            poJSON = poModel.setTranStatus(TransactionStatus.STATE_CANCELLED);
//
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            poJSON = poModel.saveRecord();
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
        }
        return poJSON;
    }


    @Override
    public JSONObject searchWithCondition(String string) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public JSONObject searchTransaction(String fsColumn, String fsValue, boolean fbByCode) {
        String lsCondition = "";
        String lsFilter = "";

        if (psTranStatus.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStatus.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStatus.charAt(lnCtr)));
            }

            lsCondition = fsColumn + " IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = fsColumn + " = " + SQLUtil.toSQL(psTranStatus);
        }

        if (!fbByCode) {
            lsFilter = fsColumn + " LIKE " + SQLUtil.toSQL(fsValue);
        } else {
            lsFilter = fsColumn + " = " + SQLUtil.toSQL(fsValue);
        }
        Model_Account_Accreditation model = new Model_Account_Accreditation(poGRider);
        String lsSQL = MiscUtil.addCondition(model.makeSQL(), lsCondition + " AND " + lsFilter + " GROUP BY sTransNox");
        
        poJSON = new JSONObject();

        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                fsValue,
                "Transaction No»Date»Name",
                "sTransNox»dTransact»sCompnyNm",
                "sTransNox»dTransact»sCompnyNm",
                fbByCode ? 0 : 1);

        if (poJSON != null) {
//            return poModel.openRecord((String) poJSON.get("sTransNox"));
            return OpenAccount((String) poJSON.get("sTransNox"));
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded to update.");
            return poJSON;
        }
    }
    
    @Override
    public JSONObject searchMaster(String string, String string1, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public JSONObject searchMaster(int i, String string, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Object getMasterModel() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public JSONObject setMaster(int i, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public JSONObject setMaster(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }
    
    public JSONObject searchRecord(String fsValue, boolean fbByCode) {
        return SearchAccredetation(fsValue, fbByCode);
    }

    @Override
    public void setTransactionStatus(String string) {
       psTranStatus = string;
    }
    
    public ArrayList<Model_Account_Accreditation> getAccount(){return poModel;}
    public void setAccount(ArrayList<Model_Account_Accreditation> foObj){poModel = foObj;}
    
    
    public JSONObject setAccount(int fnRow, int fnIndex, Object foValue){ return poModel.get(fnRow).setValue(fnIndex, foValue);}
    public JSONObject setAccount(int fnRow, String fsIndex, Object foValue){ return  poModel.get(fnRow).setValue(fsIndex, foValue);}
    public Object getAccount(int fnRow, int fnIndex){return poModel.get(fnRow).getValue(fnIndex);}
    public Object getAccount(int fnRow, String fsIndex){return poModel.get(fnRow).getValue(fsIndex);}
    
    public JSONObject searchMaster(int fnRow, String fsColumn, String fsValue, boolean fbByCode) {    
        return searchMaster(poModel.get(fnRow).getColumn(fsColumn), fsValue, fbByCode);
    }

    public JSONObject searchMaster(int fnRow, int fnColumn, String fsValue, boolean fbByCode) {
        poJSON = new JSONObject();
        switch(fnRow){
            case 14: //sClientNm
                poJSON = SearchClient(fnRow, fsValue, fbByCode);
                break;
        }
        return poJSON;
    }
    public JSONObject SearchClient(int fnRow, String fsValue, boolean fbByCode){
        String lsHeader = "ID»Name»Contact Person";
        String lsColName = "sClientID»sCompnyNm»sCPerson1";
        String lsColCrit = "a.sClientID»b.sCompnyNm»c.sCPerson1";
        String lsTable;
        if(poModel.get(fnRow).getAcctType().equalsIgnoreCase("0")){
            lsTable = "AP_Client_Master";
        }else{
            lsTable = "AR_Client_Master";
        }
        String lsSQL = "SELECT " +
                            "  a.sClientID" +
                            ", b.sCompnyNm" +
                            ", c.sCPerson1" + 
                            ", c.sContctID" + 
                            " FROM " + lsTable + " a " +
                                " LEFT JOIN Client_Master b ON a.sClientID = b.sClientID"+
                                " LEFT JOIN Client_Institution_Contact_Person c" +
                                    " ON b.sClientID = c.sClientID AND c.cPrimaryx = '1'";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sClientID = " + SQLUtil.toSQL(fsValue)) + " GROUP BY a.sClientID";
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%")) + " GROUP BY a.sClientID";
        
       
      
        JSONObject loJSON;
        String lsValue;
        System.out.println("lsSQL = " + lsSQL);
        loJSON = ShowDialogFX.Search(poGRider, 
                                        lsSQL, 
                                        fsValue, 
                                        lsHeader, 
                                        lsColName, 
                                        lsColCrit, 
                                        fbByCode ? 0 :1);
            
        System.out.println("loJSON = " + loJSON.toJSONString());
            
            if (loJSON != null && !"error".equals((String) loJSON.get("result"))) {
                System.out.println("json sClientID = " + (String) loJSON.get("sClientID"));
                lsValue = (String) loJSON.get("sClientID");
                setAccount(fnRow, "sClientID", (String) loJSON.get("sClientID"));
                setAccount(fnRow, "sContctID", (String) loJSON.get("sContctID"));
                setAccount(fnRow, "xCPerson1", (String) loJSON.get("sCPerson1"));
                setAccount(fnRow, "xCompnyNm", (String) loJSON.get("sCompnyNm"));
                
//                System.out.println("get sClientID = " + getAccount(fnRow, 4));
                loJSON.put("result", "success");
            }else {
                loJSON = new JSONObject();
                loJSON.put("result", "error");
                loJSON.put("message", "No client information found for: " + fsValue + ", Please check client type and client name details.");
                return loJSON;
            }
        return loJSON;
    }
    public JSONObject addDetail(){
        poJSON = new JSONObject();
        
        psTransNox = (MiscUtil.getNextCode(new Model_Account_Accreditation(poGRider).getTable(), "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode()));
        if (poModel.isEmpty()){
            poModel.add(new Model_Account_Accreditation(poGRider));
            poModel.get(0).newRecord();
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");
            Category loCateg = new Category(poGRider, true);
            switch (poGRider.getDivisionCode()) {
                case "0"://mobilephone
                    loCateg.openRecord("0002");
                    break;

                case "1"://motorycycle
                    loCateg.openRecord("0001");
                    break;

                case "2"://Auto Group - Honda Cars
                case "5"://Auto Group - Nissan
                case "6"://Auto Group - Any
                    loCateg.openRecord("0003");
                    break;

                case "3"://Hospitality
                case "4"://Pedritos Group
                    loCateg.openRecord("0004");
                    break;

                case "7"://Guanzon Services Office
                     break;

                case "8"://Main Office
                    break;
            }
            poModel.get(0).setCategoryCode((String) loCateg.getMaster("sCategrCd"));
            poModel.get(0).setCategoryName((String) loCateg.getMaster("sDescript"));
            

        } else {
            
            ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.Account_Accreditation, poModel.get(poModel.size()-1));
            if(!validator.isEntryOkay()){
                poJSON.put("result", "error");
//                poJSON.put("message", poModel.get(poModel.size()-1).getClientID());
                poJSON.put("message", validator.getMessage());
                return poJSON;
            }
            poModel.add(new Model_Account_Accreditation(poGRider));
            poModel.get(poModel.size()-1).newRecord();
            
            Category loCateg = new Category(poGRider, true);
            switch (poGRider.getDivisionCode()) {
                case "0"://mobilephone
                    loCateg.openRecord("0002");
                    break;

                case "1"://motorycycle
                    loCateg.openRecord("0001");
                    break;

                case "2"://Auto Group - Honda Cars
                case "5"://Auto Group - Nissan
                case "6"://Auto Group - Any
                    loCateg.openRecord("0003");
                    break;

                case "3"://Hospitality
                case "4"://Pedritos Group
                    loCateg.openRecord("0004");
                    break;

                case "7"://Guanzon Services Office
                     break;

                case "8"://Main Office
                    break;
            }
            poModel.get(poModel.size()-1).setCategoryCode((String) loCateg.getMaster("sCategrCd"));
            poModel.get(poModel.size()-1).setCategoryName((String) loCateg.getMaster("sDescript"));
            poJSON.put("result", "success");
            poJSON.put("message", "Address add record.");
        }
        return poJSON;
    }
    public JSONObject OpenAccount(String fsValue){
        Model_Account_Accreditation model = new Model_Account_Accreditation(poGRider);
        String lsSQL = MiscUtil.addCondition(model.getSQL(), "a.sTransNox = " + SQLUtil.toSQL(fsValue));
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            int lnctr = 0;
            if (MiscUtil.RecordCount(loRS) > 0) {
                poModel = new ArrayList<>();
                while(loRS.next()){
                        poModel.add(new Model_Account_Accreditation(poGRider));
                        
                        poModel.get(poModel.size() - 1).openRecord(loRS.getString("sTransNox"));
                        
                        pnEditMode = EditMode.UPDATE;
                        lnctr++;
                        poJSON.put("result", "success");
                        poJSON.put("message", "Record loaded successfully.");
                    } 
                
                System.out.println("lnctr = " + lnctr);
                
            }else{
                poModel = new ArrayList<>();
                addDetail();
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record selected.");
            }
            
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return poJSON;
    }
    
    private JSONObject saveRecord(String type){
        
        JSONObject obj = new JSONObject();
        if (poModel.size()<= 0){
            obj.put("result", "error");
            obj.put("message", "No client address detected. Please encode client address.");
            return obj;
        }
        
        int lnCtr;
        String lsSQL;
        Model_Account_Accreditation loModel = new Model_Account_Accreditation(poGRider);
//        String lsTransNox = MiscUtil.getNextCode(loModel.getTable(), "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode());        
        for (lnCtr = 0; lnCtr <= poModel.size() -1; lnCtr++){
            if("Cancel".equals(type)){
                obj = poModel.get(lnCtr).setTranStatus(TransactionStatus.STATE_CANCELLED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }else if("Void".equals(type)){
                obj = poModel.get(lnCtr).setTranStatus(TransactionStatus.STATE_VOID);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
            }else if("Post".equals(type)){
                obj = poModel.get(lnCtr).setTranStatus(TransactionStatus.STATE_POSTED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }else if("Close".equals(type)){
                obj = poModel.get(lnCtr).setTranStatus(TransactionStatus.STATE_CLOSED);
//
                if ("error".equals((String) obj.get("result"))) {
                    return obj;
                }
                
            }
            ValidatorInterface validator = ValidatorFactory.make(ValidatorFactory.TYPE.Account_Accreditation, poModel.get(lnCtr));
            poModel.get(lnCtr).setModifiedDate(poGRider.getServerDate());
//            poModel.get(lnCtr).setTransactionNo(lsTransNox);
            
            if (!validator.isEntryOkay()){
                obj.put("result", "error");
                obj.put("message", validator.getMessage());
                return obj;
            
            }
            obj = poModel.get(lnCtr).saveRecord();

        }    
        
        return obj;
    }
    public JSONObject SearchCategory(int fnRow, String fsValue, boolean fbByCode){
        
        JSONObject loJSON;
        Category loCategory = new Category(poGRider, true);
        loCategory.setRecordStatus(psTranStatus);
        loJSON = loCategory.searchRecord(fsValue, fbByCode);

        if (loJSON != null){
//                setMaster("sCategrCd", (String) loJSON.get("sCategrCd"));
//                setMaster("xCategrNm", (String) loJSON.get("sDescript"));
//            setMaster(fnCol, (String) loCategory.getMaster("sCategrCd"));
//                    setMaster("xCategNm1", (String)loCategory.getMaster("sDescript"));

            poModel.get(fnRow).setCategoryCode((String) loCategory.getMaster("sCategrCd"));
            poModel.get(fnRow).setCategoryName((String) loCategory.getMaster("sDescript"));
            return setAccount(fnRow, "xCategrNm", (String)loCategory.getMaster("sDescript"));
        } else {
            loJSON = new JSONObject();
            loJSON.put("result", "error");
            loJSON.put("message", "No Category information found for: " + fsValue + ", Please check Catergory Code and Description details.");
            return loJSON;
        }
    }
//    
//    public JSONObject SearchCategory(int fnRow, String fsValue, boolean fbByCode){
//         JSONObject loJSON;
//        Category loCategory = new Category(poGRider, true);
//        loCategory.setRecordStatus(psTranStatus);
//        loJSON = loCategory.searchRecord(fsValue, fbByCode);
//
//            
//        if (loJSON != null && !"error".equals((String) loJSON.get("result"))) {
////            System.out.println("json sCategrCd = " + (String) loJSON.get("sCategrCd"));
//////                lsValue = (String) loJSON.get("sCategrCd");
////            setAccount(fnRow, "sCategrCd", (String) loJSON.get("sCategrCd"));
//////                setAccount(fnRow, "sDescript", (String) loJSON.get("sDescript"));
////
//////                System.out.println("get sClientID = " + getAccount(fnRow, 4));
////            loJSON = setAccount(fnRow, "xCategrNm", (String) loJSON.get("sDescript"));
//            
//            setAccount(fnRow, "sCategrCd", (String) loCategory.getMaster("sCategrCd"));
////                setMaster("xCategrNm", (String) loJSON.get("sDescript"));
////            setMaster(fnCol, (String) loCategory.getMaster("sCategrCd"));
////                    setMaster("xCategNm1", (String)loCategory.getMaster("sDescript"));
//
//            return setAccount(fnRow, "xCategrNm", (String)loCategory.getMaster("sDescript"));
//        }else {
//            loJSON.put("result", "error");
//            loJSON.put("message", "No Category information found for: " + fsValue + ", Please check Catergory Code and Description details.");
//            return loJSON;
//        }
////        return loJSON;
//    }
    
    public JSONObject SearchAccredetation(String fsValue, boolean fbByCode){
        String lsHeader = "Transaction No»Company Name»Date";
        String lsColName = "sTransNox»sCompnyNm»dTransact";
        String lsColCrit = "a.sTransNox»b.sCompnyNm»a.dTransact";
        String lsSQL = " SELECT " +
                        " a.sTransNox, " +
                        " a.cAcctType, " +
                        " a.sClientID, " +
                        " a.dTransact, " +
                        " a.cAcctType, " +
                        " a.sRemarksx, " +
                        " a.cTranType, " +
                        " a.sCategrCd, " +
                        " a.cTranStat, " +
                        " b.sCompnyNm " +
                      " FROM Account_Client_Acccreditation a " +
                        " LEFT JOIN Client_Master b " +
                          " on a.sClientID = b.sClientID " +
                        " LEFT JOIN client_address c " +
                          " on a.sClientID = c.sClientID " +
                        " LEFT JOIN Client_Institution_Contact_Person d " +
                          " on d.sClientID = a.sClientID " ;
                        
        if (fbByCode){
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsValue + "%") + " GROUP BY a.sTransNox");
        }else{
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%")+ " GROUP BY a.sTransNox");
        }
        lsSQL = MiscUtil.addCondition(lsSQL, "a.cAcctType = " + SQLUtil.toSQL(psAccountType));
        
       
           
        System.out.println("lsSQL = " + lsSQL);
        JSONObject loJSON;
        String lsValue;
            
            
       
        System.out.println("lsSQL = " + lsSQL);
        loJSON = ShowDialogFX.Search(poGRider, 
                                        lsSQL, 
                                        fsValue, 
                                        lsHeader, 
                                        lsColName, 
                                        lsColCrit, 
                                        fbByCode ? 0 :1);
            
//        System.out.println("loJSON = " + loJSON.toJSONString());
            
            if (loJSON != null && !"error".equals((String) loJSON.get("result"))) {
                System.out.println("sTransNox = " + (String) loJSON.get("sTransNox"));
                
                return openTransaction((String) loJSON.get("sTransNox"));
            }else {
                loJSON.put("result", "error");
                loJSON.put("message", "No client information found for: " + fsValue + ", Please check client type and client name details.");
                return loJSON;
            }
    }
    
}
