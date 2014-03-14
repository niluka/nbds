/*
 * MSc(Biomedical Informatics) Project
 * 
 * Development and Implementation of a Web-based Combined Data Repository of 
 Genealogical, Clinical, Laboratory and Genetic Data 
 * and
 * a Set of Related Tools
 */
package gov.health.bean;


import gov.health.facade.DepartmentFacade;
import gov.health.entity.Department;
import gov.health.entity.Institution;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class DepartmentController implements Serializable {

    @EJB
    private DepartmentFacade facade;
    

    @Inject
    SessionController sessionController;
   
   
    private Department current;
    private List<Department> items = null;
    String selectText = "";
    
    Institution institution;
    String depaertmentName;
    
    
    public void addDepartmentForInstitution(){
        if(institution == null){
            JsfUtil.addErrorMessage("Please select the Institution");
               return;
        }
        if(depaertmentName.trim().equals("")){
          JsfUtil.addErrorMessage("Please enter the name of the Department");
          return;
        }
        Department departmentAdd = new Department();
        departmentAdd.setInstitution(institution);
        departmentAdd.setName(depaertmentName);
        getFacade().create(departmentAdd);
        JsfUtil.addSuccessMessage("Saved");
        
    }
            
            
    public void saveDepartment(Department ins) {
        if (ins == null) {
            JsfUtil.addErrorMessage("Nothing to update");
            return;
        }
        if (ins.getId() == null || ins.getId() == 0) {
            getFacade().create(ins);
            JsfUtil.addSuccessMessage("Saved");
        } else {
            getFacade().edit(ins);
            JsfUtil.addSuccessMessage("Updated");
        }
    }

 



    public Department findDepartment(String insName, boolean createNew) {
        insName = insName.trim();
        if (insName.equals("")) {
            return null;
        }
        Department ins = getFacade().findFirstBySQL("select d from Department d where d.retired = false and lower(d.name) = '" + insName.toLowerCase() + "'");
        if (ins == null && createNew == true) {
            ins = new Department();
            ins.setName(insName);
            ins.setCreatedAt(Calendar.getInstance().getTime());
            ins.setCreater(sessionController.loggedUser);
            getFacade().create(ins);
        }
        return ins;
    }

   
    public DepartmentController() {

    }


    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getDepaertmentName() {
        return depaertmentName;
    }

    public void setDepaertmentName(String depaertmentName) {
        this.depaertmentName = depaertmentName;
    }

   

   


    public Department getCurrent() {
        if (current == null) {
            current = new Department();
            
        }
        return current;
    }

    public void setCurrent(Department current) {
        if (this.current != current) {
        }
        this.current = current;
    }

    private DepartmentFacade getFacade() {
        return facade;
    }

    public List<Department> getItems() {
        String temSql;
        if (items != null) {
            return items;
        }
        if (getSelectText().equals("")) {
           
                temSql = "SELECT i FROM Department i where i.retired=false order by i.name";
           
        } else {
            
                temSql = "SELECT i FROM Department i where i.retired=false and LOWER(i.name) like '%" + getSelectText().toLowerCase() + "%' order by i.name";
            
        }
        items = getFacade().findBySQL(temSql);
        //System.out.println(temSql);
        return items;
    }

    public void setFacade(DepartmentFacade facade) {
        this.facade = facade;
    }

    public void setItems(List<Department> items) {
        this.items = items;
    }

    public static int intValue(long value) {
        int valueInt = (int) value;
        if (valueInt != value) {
            throw new IllegalArgumentException(
                    "The long value " + value + " is not within range of the int type");
        }
        return valueInt;
    }



    public void saveSelected() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        
        if (current.getId() != null && current.getId() != 0) {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(new MessageProvider().getValue("savedOldSuccessfully"));
        } else {
            current.setCreatedAt(Calendar.getInstance().getTime());
            current.setCreater(sessionController.loggedUser);
            getFacade().create(current);

            JsfUtil.addSuccessMessage(new MessageProvider().getValue("savedNewSuccessfully"));
        }
        
        getItems();
        selectText = "";
    }

    
   

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(Calendar.getInstance().getTime());
            current.setRetirer(sessionController.loggedUser);
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(new MessageProvider().getValue("deleteSuccessful"));
        } else {
            JsfUtil.addErrorMessage(new MessageProvider().getValue("nothingToDelete"));
        }
       
        getItems();
        selectText = "";
        current = null;
        
    }

    public String getSelectText() {
        return selectText;
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;

    }

    public List<Department> completeOfficialDepartments(String qry) {
        String temSql;
        List<Department> ins;
        temSql = "SELECT i FROM Department i where i.retired=false and i.official = true and LOWER(i.name) like '%" + qry.toLowerCase() + "%' order by i.name";
        ins = getFacade().findBySQL(temSql);
        return ins;
    }

    public List<Department> completePayCentres(String qry) {
        String temSql;
        List<Department> ins;
        temSql = "SELECT i FROM Department i where i.retired=false and i.payCentre = true and LOWER(i.name) like '%" + qry.toLowerCase() + "%' order by i.name";
        ins = getFacade().findBySQL(temSql);
        return ins;
    }

   

    @FacesConverter(forClass = Department.class)
    public static class DepartmentControllerConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DepartmentController controller = (DepartmentController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "departmentController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuffer sb = new StringBuffer();
            sb.append(value);
            return sb.toString();
        }

        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Department) {
                Department o = (Department) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + DepartmentController.class.getName());
            }
        }
    }

    @FacesConverter("departmentConverter")
    public static class DepartmentConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DepartmentController controller = (DepartmentController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "departmentController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuffer sb = new StringBuffer();
            sb.append(value);
            return sb.toString();
        }

        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Department) {
                Department o = (Department) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + DepartmentController.class.getName());
            }
        }
    }
}
