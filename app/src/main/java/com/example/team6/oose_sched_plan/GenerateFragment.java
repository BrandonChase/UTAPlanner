package com.example.team6.oose_sched_plan;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GenerateFragment extends Fragment {


    @InjectView(R.id.spin_term)
    Spinner spinTerm;
    @InjectView(R.id.spin_year)
    Spinner spinYear;
    @InjectView(R.id.list_available)
    ListView listAvailable;
    @InjectView(R.id.c_info_dep_number_field)
    TextView cInfoDepNumberField;
    @InjectView(R.id.c_info_coursename_field)
    TextView cInfoCoursenameField;
    @InjectView(R.id.c_info_descript_field)
    TextView cInfoDescriptField;
    @InjectView(R.id.c_info_coursecredit_field)
    TextView cInfoCoursecreditField;
    @InjectView(R.id.button_add)
    Button buttonAdd;
    @InjectView(R.id.list_current)
    ListView listCurrent;
    @InjectView(R.id.c_info_creditcategory_field)
    TextView cInfoCreditcategoryField;

    private Spinner spinner_term, spinner_year;
    private ArrayAdapter<String> dataAdapter;
    private ListView listview_available, listview_current;
    private Button button_add;
    private TextView cInfo_DepartmentNumber, cInfo_CourseName, cInfo_Description, cInfo_credit, cInfo_CreditCategory;
    private View row;

    // This list_dynam_Available is used to store data to be passed between methods
    //  This will be replaced with a class
    private ArrayList<HashMap<String, String>> list_dynam_Available = new ArrayList<>();
    private ArrayList<HashMap<String, String>> list_dynam_Current = new ArrayList<>();

    //TODO USING COURSE CLASS
    private ArrayList<Course> list_course_available = new ArrayList<Course>();
    private ArrayList<Course> list_course_current;

    //store selected items
    private HashMap<String, String> selected_item;
    private int selected_position;

    // Saving preferences
    //SharedPreferences sharedPreferences;
    //SharedPreferences.Editor collection;
    Gson gson = new Gson();

    //Database
    DegreePlanAdapter.FeedReaderDbHelper mDbHelper;
    SQLiteDatabase db;

    //Schedule
    Schedule schedule;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_generate, container, false);
        ButterKnife.inject(this, view);
        TinyDB tinydb = new TinyDB(view.getContext());

        //sharedPreferences = view.getContext().getSharedPreferences(Config.SHARED_PREF_NAME, view.getContext().MODE_PRIVATE);
        addItemsOnSpinTerm();
        addItemsOnSpinYear();


        addItemsOnCurrentSchedule();

        button_add = buttonAdd;
        button_add.setClickable(true);
        button_add.setBackgroundResource(R.drawable.b_button_deselected);

        alertDegreePlan(view, tinydb);
//        alertDegreePlan(view,tinydb);
//        if (tinydb.getString(Config.SHARED_DEGREE_PLAN).equals(""))
//        {
//
//        }
//        else{
//            Toast toast = Toast.makeText(view.getContext(), tinydb.getString(Config.SHARED_DEGREE_PLAN), Toast.LENGTH_SHORT);
//            toast.show();
//            String string = tinydb.getString(Config.SHARED_AVAILABLE_COURSELIST);
//            toast = Toast.makeText(view.getContext(), string, Toast.LENGTH_LONG);
//            toast.show();
//        }
        addItemsOnAvailable(view);

        return view;
    }

    /* ================================================================
    ||  TERM - Gets info and populates "term" dropdown spinner.
    ||  - Currently added from list_dynam_Available and not database
    ||=================================================================*/
    public void addItemsOnSpinTerm() {
        spinner_term = spinTerm;
        List<String> list_terms = new ArrayList<String>();
        list_terms.add("Spring");
        list_terms.add("Summer");
        list_terms.add("Fall");
        dataAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, list_terms);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_term.setAdapter(dataAdapter);
    }

    /* ================================================================
    ||  TERM YEAR - Gets info and populates "Year" dropdown spinner.
    ||  - Currently added from list_dynam_Available and not database
    ||=================================================================*/
    public void addItemsOnSpinYear() {
        spinner_year = spinYear;
        List<String> list_years = new ArrayList<String>();
        list_years.add("2017");
        list_years.add("2018");
        list_years.add("2019");
        list_years.add("2020");
        list_years.add("2021");
        dataAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, list_years);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_year.setAdapter(dataAdapter);
    }

    /* ================================================================
    ||  AVAILABLE - Gets info and populates "Available courses" list_dynam_Available. Allows user to select Course to update COURSE INFO;
    ||  - Currently added from list_dynam_Available and not database
    ||  - Only department/#, course name implemented
    ||=================================================================*/
    public void addItemsOnAvailable(final View view) {
        listview_available = listAvailable;

        String[] from = new String[]{"course", "name"};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        int nativeLayout = android.R.layout.two_line_list_item;
        listview_available.setAdapter(new SimpleAdapter(this.getContext(), list_dynam_Available, nativeLayout, from, to));
        listview_available.setClickable(true);
        listview_available.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                addItemsOnCourseInfo(list_dynam_Available, position);
                selected_item = list_dynam_Available.get(position);
                selected_position = position;

                if (row != null) {
                    row.setBackgroundResource(R.color.alpha);
                }
                row = v;
                v.setBackgroundResource(R.drawable.b_selected);

                addListenerOnAddButton();
            }
        });
    }

    /* ================================================================
    ||  COURSE INFO - Gets info from course selected,
    ||=================================================================*/
    public void addItemsOnCourseInfo(ArrayList<HashMap<String, String>> list, int position) {
        String department = list.get(position).get("course");
        String name = list.get(position).get("name");
        String description = list.get(position).get("description");
        String creditCategory = list.get(position).get("creditcategory");
        String creditHours = list.get(position).get("credithours");

        cInfo_DepartmentNumber = cInfoDepNumberField;
        cInfo_CourseName = cInfoCoursenameField;
        cInfo_Description = cInfoDescriptField;
        cInfo_credit = cInfoCoursecreditField;
        cInfo_CreditCategory = cInfoCreditcategoryField;

        cInfo_DepartmentNumber.setText(department);
        cInfo_CourseName.setText(name);
        cInfo_Description.setText(description);
        cInfo_credit.setText(creditHours);
        cInfo_CreditCategory.setText(creditCategory);
    }

    /* ================================================================
    ||  ADD Button - gets the SELECTED ITEM and puts it into the current schedule.
    ||  - does not do any validation currently
    ||=================================================================*/
    public void addListenerOnAddButton() {
        button_add = buttonAdd;
        button_add.setText(R.string.button_add);
        button_add.setBackgroundResource(R.drawable.b_button_selected);
        button_add.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View arg0) {
                if (selected_item != null) {
                    list_dynam_Current.add(selected_item);
                    list_dynam_Available.remove(selected_position);
                    String[] from = new String[]{"course", "name"};
                    int nativeLayout = android.R.layout.two_line_list_item;
                    int[] to = new int[]{android.R.id.text1, android.R.id.text2};
                    listview_current.setAdapter(new SimpleAdapter(arg0.getContext(), list_dynam_Current, nativeLayout, from, to));
                    listview_available.setAdapter(new SimpleAdapter(arg0.getContext(), list_dynam_Available, nativeLayout, from, to));
                    listview_current.setClickable(true);
                    selected_item = null;
                    button_add.setBackgroundResource(R.drawable.b_button_deselected);
                }
            }
        });
    }

    //change to remove button
    public void addListenerOnRemoveButton() {
        button_add = buttonAdd;
        button_add.setText(R.string.button_remove);
        button_add.setBackgroundResource(R.drawable.b_button_remove);
        button_add.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View arg0) {
                if (selected_item != null) {
                    list_dynam_Available.add(selected_item);
                    list_dynam_Current.remove(selected_position);
                    String[] from = new String[]{"course", "name"};
                    int nativeLayout = android.R.layout.two_line_list_item;
                    int[] to = new int[]{android.R.id.text1, android.R.id.text2};
                    listview_current.setAdapter(new SimpleAdapter(arg0.getContext(), list_dynam_Current, nativeLayout, from, to));
                    listview_available.setAdapter(new SimpleAdapter(arg0.getContext(), list_dynam_Available, nativeLayout, from, to));
                    listview_current.setClickable(true);
                    selected_item = null;
                    button_add.setBackgroundResource(R.drawable.b_button_deselected);

                }

            }
        });
    }

    /* ================================================================
    ||  ADD TO CURRENT SCHEDULE - initializes values and has listener for list item clicks
    ||=================================================================*/
    public void addItemsOnCurrentSchedule() {
        listview_current = listCurrent;
        String[] from = new String[]{"course", "name"};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        int nativeLayout = android.R.layout.two_line_list_item;

        listview_current.setAdapter(new SimpleAdapter(this.getContext(), list_dynam_Current, nativeLayout, from, to));
        listview_current.setClickable(true);

        listview_current.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {

                //Get info from spinner term/ year
                //TODO additional logic checking?
                int spin_year = Integer.parseInt(spinner_year.getSelectedItem().toString());
                Term spin_term = Term.valueOf(spinner_term.getSelectedItem().toString());

                // Add the course to the SCHEDULE
                schedule.addCourse(spin_term, spin_year, list_course_available.get(position));

                // Update the COURSE INFO
                addItemsOnCourseInfo(list_dynam_Current, position);
                selected_item = list_dynam_Current.get(position);
                selected_position = position;

                if (row != null) {
                    row.setBackgroundResource(R.color.alpha);
                }
                row = v;
                v.setBackgroundResource(R.drawable.b_selected);
                addListenerOnRemoveButton();

            }
        });
    }

    /* ================================================================
    ||  DEGREE PLAN - Alerts user with popup for Degree Plans.
    ||=================================================================*/
    public void alertDegreePlan(final View view, final TinyDB tinydb) {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(this.getContext());
        builderSingle.setTitle("Select a Degree Plan: ");
        final ArrayAdapter<String> list_majors = new ArrayAdapter<String>(this.getContext(), android.R.layout.select_dialog_singlechoice);
        list_majors.add("CSE - Computer Science Engineering");
        list_majors.add("SE - Software Engineering");
        list_majors.add("CPE - Computer Engineering");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(list_majors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Create new schedule
                schedule = new Schedule();

                //Delete all entries to avoid duplicates
                DegreePlanInfo.DeleteAllEntries(view);

                mDbHelper = new DegreePlanAdapter.FeedReaderDbHelper(view.getContext());
                db = mDbHelper.getWritableDatabase();
                DegreePlanInfo.PopulateDatabase(db);

                // Uses array list Courses enum
                list_course_available = schedule.generateAvailableCourses(Term.Spring, 2017, mDbHelper);
                list_dynam_Available = QueryToListView(list_course_available, list_dynam_Available);

//                Save degreePicked to tinyDB
//                String degreePicked = list_majors.getItem(which);
//                tinydb.putString(Config.SHARED_DEGREE_PLAN, degreePicked);
//
//                Store dynamic list to json object
//                avail_to_json = gson.toJson(list_dynam_Available);
//                tinydb.putObject(Config.SHARED_AVAILABLE_COURSELIST, avail_to_json);
//                tinydb.putString(Config.SHARED_AVAILABLE_COURSELIST, avail_to_json);

                //Update list
                String[] from = new String[]{"course", "name"};
                int[] to = new int[]{android.R.id.text1, android.R.id.text2};
                int nativeLayout = android.R.layout.two_line_list_item;

                // LOADS available list of courses
                listview_available.setAdapter(new SimpleAdapter(view.getContext(), list_dynam_Available, nativeLayout, from, to));

                //TODO do multiple degree plans, not just CSE
                /*
                switch (which)
                {
                    case 1:
                        list_dynam_Available = DegreePlanInfo.QueryDegreePlans(view.getContext(), mDbHelper,db, list_dynam_Available );
                }
                */

                mDbHelper.close();
            }
        });
        builderSingle.show();
    }

    /* ================================================================
    ||  FUNCTION TO CONVERT - ARRAY LIST<COURSE> -->  ARRAY_LIST<HASHMAP<STRING,STRING>>
    ||=================================================================*/
    public ArrayList<HashMap<String, String>> QueryToListView(ArrayList<Course> course_list, ArrayList<HashMap<String, String>> list_course_hashmap) {
        for (Course course : course_list) {
            HashMap<String, String> course_hashmap = new HashMap<>();
            course_hashmap.put("course", String.valueOf(course.department) + String.valueOf(course.number));
            course_hashmap.put("name", String.valueOf(course.name));
            course_hashmap.put("description", String.valueOf(course.description));
            course_hashmap.put("creditcategory", String.valueOf(course.creditCategory));
            course_hashmap.put("credithours", String.valueOf(course.creditHours));
            list_course_hashmap.add(course_hashmap);
        }
        return list_course_hashmap;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
