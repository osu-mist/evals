package edu.osu.cws.evals.testdata;

import edu.osu.cws.evals.hibernate.ClassifiedITObjectMgr;
import edu.osu.cws.evals.models.ClassifiedITObject;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.portlet.AmtObject;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.evals.util.*;
import edu.osu.cws.util.CWSUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;


import java.text.MessageFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 6/27/12
 * Time: 10:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class InsertObjectTest {

    private static final int MAX_SIZE = 20;

    private static int PId = 1 ;
    private static int OsuId = 900000000;
    private static ArrayList<AmtObject> amtObjects = new ArrayList<AmtObject>();
    private static ArrayList<String> SAL_Grade = new ArrayList<String>();

    public static void InitList(){
        RandomNumberString randomNumberString = new RandomNumberString(4);
        //RandomNumberString randomNumberStringS = new RandomNumberString(3);
        amtObjects.add(new AmtObject("Classified","C3"+randomNumberString.nextString(),"CA"));
        amtObjects.add(new AmtObject("Classified","C3"+randomNumberString.nextString(),"CB"));
        amtObjects.add(new AmtObject("Classified IT","C3"+randomNumberString.nextString(),"CA"));
        amtObjects.add(new AmtObject("Student","C5"+randomNumberString.nextString(),"XA"));
       // amtObjects.add(new AmtObject("Student","C55"+randomNumberStringS.nextString(),"XA"));
        amtObjects.add(new AmtObject("Unknown","C4"+randomNumberString.nextString(),"TS"));
        amtObjects.add(new AmtObject("Ranked Faculty","C1"+randomNumberString.nextString(),"UA"));
        amtObjects.add(new AmtObject("Ranked Faculty","C1"+randomNumberString.nextString(),"UC"));
        amtObjects.add(new AmtObject("Ranked Faculty","C1"+randomNumberString.nextString(),"UB"));
        amtObjects.add(new AmtObject("Ranked Faculty","C1"+randomNumberString.nextString(),"UD"));
        amtObjects.add(new AmtObject("Professional Faculty","C1"+randomNumberString.nextString(),"UE"));
        amtObjects.add(new AmtObject("Professional Faculty","C1"+randomNumberString.nextString(),"UG"));
        amtObjects.add(new AmtObject("Professional Faculty","C1"+randomNumberString.nextString(),"UF"));
        amtObjects.add(new AmtObject("Professional Faculty","C1"+randomNumberString.nextString(),"UH"));
        SAL_Grade.add("32I-2");
        SAL_Grade.add("28I-1");
        SAL_Grade.add("28I-2");
        SAL_Grade.add("34I-3");
        SAL_Grade.add("26I-1");
        SAL_Grade.add("32I-2");
        SAL_Grade.add("32I-3");
        SAL_Grade.add("34I-2");
        SAL_Grade.add("32I-1");


    }


    public static void Insert(){
        Session hibSession = null;
        Employee employee = null;
        hibSession = HibernateUtil.getCurrentSession();
        Transaction tx = hibSession.beginTransaction();
        for(int i =0; i<MAX_SIZE ;i++){
            employee = CreateEmployee(hibSession);
            CreatJobs(employee,hibSession);
            hibSession.save(employee);
        }
        tx.commit();

    }

    private static Employee CreateEmployee(Session hibSession){

        RandomString randomString = new RandomString(6);
        PId = getPid(getCurrentPid(hibSession),hibSession);
        Employee employee = new Employee();
        employee.setId(PId);
        employee.setOnid(getOnid(hibSession));
        OsuId = getOsuId(getCurrentOsuId(hibSession),hibSession);
        employee.setOsuid(Integer.toString(OsuId));
        employee.setFirstName(randomString.nextString());
        employee.setLastName(randomString.nextString());
        employee.setMiddleName("CWS created");
        employee.setEmail("joan.lu@oregonstate.edu");
        employee.setStatus("A");
        return employee;
    }

    private static int getCurrentPid(Session hibSession){
        String query = "select max(employee.id) from edu.osu.cws.evals.models.Employee employee";
        Iterator max = hibSession.createQuery(query).iterate();
        int currentPid = Integer.parseInt(max.next().toString());
        return currentPid;
    }

    private static int getPid(int currentPid, Session hibSession){
        int OutPidm = -1;
        while (OutPidm == -1){
            int count = hibSession.createQuery (
                    "from edu.osu.cws.evals.models.Employee employee where employee.id = ?") .
                    setInteger(0,currentPid).list().size();
            if (count == 0){
                OutPidm = currentPid;
            }
            else  currentPid++;
        }
        System.out.println("getPid"+currentPid);
        return  OutPidm;
    }


    private static String getOnid(Session hibSession) {
        String OutOnid  = null;
        RandomString randomString = new RandomString(8);
        String CurrentOnid = null;
        while (OutOnid == null){
            CurrentOnid = randomString.nextString();
            int count = hibSession.createQuery (
                    "from edu.osu.cws.evals.models.Employee employee where employee.onid = ?") .
                    setString(0, CurrentOnid).list().size();
            if (count == 0){
                OutOnid = CurrentOnid;
            }

        }
        System.out.println("getOnid");
        return  OutOnid;
    }

    private static int getCurrentOsuId(Session hibSession){
        String query = "select max(employee.osuid) from edu.osu.cws.evals.models.Employee employee";
        Iterator max = hibSession.createQuery(query).iterate();
        int currentOsuId = Integer.parseInt(max.next().toString());
        return currentOsuId;
    }

    private static int getOsuId (int CurrentOsuId, Session hibSession){
        int OutOsuId = -1;
        while (OutOsuId == -1){
            String idString = Integer.toString(CurrentOsuId);
            long count = (Long)hibSession.createQuery (
                    "select count(*) from edu.osu.cws.evals.models.Employee employee where employee.osuid = ?").
                    setString(0,idString).iterate().next();
            if (count == 0){
                OutOsuId = CurrentOsuId;
            }
            else  CurrentOsuId++;
        }
        System.out.println("getOsuId");
        return  OutOsuId;
    }

    public static void CreatJobs(Employee employee,Session hibSession){
        Random random = new Random();
        Job job;
        int count = random.nextInt(3) + 1;       //employees at least have one job for each
        for(int i=0;i<count;i++){
            job = setNewJob(hibSession);
            while (1==1){
                for(Object obj : employee.getJobs()){
                    Job jobInList = (Job)obj;
                    if(job.getPositionNumber().equals(jobInList.getPositionNumber())){
                        job = setNewJob(hibSession);
                        break;
                    }
                }
                break;
            }
            job.setEmployee(employee);
            employee.getJobs().add(job);
            hibSession.save(job);
        }
    }



    public static Job setNewJob(Session hibSession){
        Random random = new Random();
        AmtObject amtObject = amtObjects.get(random.nextInt(12));

        RandomString randomString = new RandomString(6);
        RandomNumberString randomNumberStringShort = new RandomNumberString(3);
        RandomNumberString randomNumberStringLong = new RandomNumberString(4);
        String posn = amtObject.getPosn();
        Job job = new Job();
        if(posn.substring(0,2).equals("C55")){
              String str= randomNumberStringShort.nextString() ;
              posn = posn.substring(0,3)+str;
        }

        else {
              String str= randomNumberStringLong.nextString() ;
              posn = posn.substring(0,2)+str;
        }
        job.setAppointmentType(amtObject.getAppointment());
        job.setJobEcls(amtObject.getElcs());
        job.setPositionNumber(posn);
        job.setSuffix("00");
        job.setStatus("A");
        job.setJobTitle("CWS Created "+randomString.nextString());
        job.setBeginDate(RanDate());
        job.setPositionClass("TESTT");
        String appointmentType = job.getAppointmentType();
        if(!appointmentType.equals("Classified IT")) {
            job.setSalaryGrade("22");
        }
        else {
            job.setSalaryGrade(SAL_Grade.get(random.nextInt(8)));
        }
        job.setAnnualInd(12);
        job.setTrialInd(6);
        job.setSalaryStep("0");
        List <Job> jobs = hibSession.createQuery("FROM edu.osu.cws.evals.models.Job as job where job.jobEcls like 'U%'  "
        ).list();
        int size = jobs.size();
        Job supvisor = jobs.get(random.nextInt(size-1));
        job.setBusinessCenterName(supvisor.getBusinessCenterName());
        job.setTsOrgCode(supvisor.getTsOrgCode());
        job.setOrgCodeDescription(supvisor.getOrgCodeDescription());
        job.setSupervisor(supvisor);
        return job;
    }

    public static Date RanDate(){
        RandomDateGenerator randomDateGenerator = new RandomDateGenerator();
        Calendar calendar = Calendar.getInstance();
        calendar.set(1995,1,1);

        randomDateGenerator.setFrom(calendar.getTime());
        calendar.set(2012,12,30);
        randomDateGenerator.setTo(calendar.getTime());
        return randomDateGenerator.getRandomDateBetween();
    }

    public static void Display()  {

        Session hibSession = HibernateUtil.getCurrentSession();
        Transaction tx = hibSession.beginTransaction();
        Employee employee = (Employee) hibSession.createQuery(
                "from edu.osu.cws.evals.models.Employee as employee where employee.id = ? and status = 'A'")
                .setInteger(0, PId).uniqueResult();
        System.out.println(employee.getId());
        Set <Job> jobs = employee.getJobs();
        for (Job job:jobs)
        {
            System.out.println(job.getSupervisor().getEmployee().getId());
        }

        tx.commit();

    }

    public static void fetchClassifiedIT(Integer pidm) throws Exception{
        Session hibSession = HibernateUtil.getCurrentSession();
        Transaction tx = hibSession.beginTransaction();
        Criteria criteria = hibSession.createCriteria(Job.class);

        criteria.add(Restrictions.eq("supervisor.employee.id", pidm)).add(Restrictions.eq("status", "A")).add(Restrictions.like("appointmentType", "Classified IT"));
        List result = criteria.list();
        ArrayList<ClassifiedITObject> myTeamClassifiedITObject = new ArrayList<ClassifiedITObject>();
        String reviewPeriod = "";
        String name = "";
        if (result.isEmpty()) {
            return;
        }
        for (Object jResult : result) {
            Job job = (Job) jResult;

            job.setAnnualInd(Constants.ANNUAL_IND);
            Date startDate, endDate;
            Calendar startCal = job.getNewAnnualStartDate();
            startDate = startCal.getTime();
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            if (startDate.before(EvalsUtil.getEvalsStartDate())){
                System.out.println("before");
                startCal.set(Calendar.YEAR, currentYear);
                startDate = startCal.getTime();
            }
            endDate = job.getEndEvalDate(startDate, "annual");


            name = job.getEmployee().getName();
            reviewPeriod = getReviewPeriod(startDate, endDate);
            System.out.println(name);
            System.out.println(reviewPeriod);
            reviewPeriod = getReviewPeriod(EvalsUtil.getEvalsStartDate(), endDate);
            System.out.println(reviewPeriod);

        }
        tx.commit();
    }
    public static String getReviewPeriod(Date startDate,Date endDate) {
        if (startDate == null) {
            startDate = new Date();
        }
        if (endDate == null) {
            startDate = new Date();
        }

        return MessageFormat.format("{0,date,MM/dd/yy} - {1,date,MM/dd/yy}",
                new Object[]{startDate, endDate});
    }

    public static void fetchConfig() {

        Session hibSession = HibernateUtil.getCurrentSession();
        Transaction tx = hibSession.beginTransaction();
        Configuration configuration = (Configuration) hibSession.createQuery(
                "from edu.osu.cws.evals.models.Configuration as configration where configration.name like 'firstGoalDueReminder'").uniqueResult() ;
        System.out.println(configuration.getIntValue());
        tx.commit();
    }

    public static void main(String [] args) throws Exception
    {
        HibernateUtil.setConfig("hibernate-annie.cfg.xml");
        //InitList();
        //Insert();
        //Display();
        //fetchConfig();
        //fetchClassifiedIT(1318628);
      //  System.out.println(RandomStringUtils.random(6,false,true));
        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();
        System.out.println(d1);
        calendar.add(Calendar.DATE, 40);
        Date d2 = calendar.getTime();
        System.out.println(d2);
        System.out.println(CWSUtil.daysBetween(d2,d1));


    }


}

