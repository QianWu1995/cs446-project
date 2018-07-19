package com.macrohard.cooklit.model.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;

import com.macrohard.cooklit.R;
import com.macrohard.cooklit.database.model.Recipe;
import com.macrohard.cooklit.database.model.RecipeViewModel;
import com.macrohard.cooklit.support.adapters.TwoTextItemListViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MealPlanActivity extends AppCompatActivity {

    private CalendarView mealPlanCalender;
    private ListView mealsForDayList;
    private RecipeViewModel mRecipeViewModel;
    private List<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        mealPlanCalender = findViewById(R.id.mealPlanCalendar);
        mealPlanCalender.setFirstDayOfWeek(2);
        mealsForDayList = findViewById(R.id.mealsForDay);

        mRecipeViewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);

        getScheduleInfo(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), 0 , 0 ,0);

        mealPlanCalender.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                int dayOfWeek = getDayOfWeek(i,i1,i2);
                getScheduleInfo(dayOfWeek, i2, i1, i);
            }
        });

        mealsForDayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent recipeIntent = new Intent(MealPlanActivity.this, RecipeActivity.class);
                recipeIntent.putExtra("uri",recipes.get(i).getUri());
                startActivity(recipeIntent);
            }
        });

        mealsForDayList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final int index = i;
                AlertDialog.Builder builder = new AlertDialog.Builder(MealPlanActivity.this);
                builder.setTitle("Possible Actions")
                        .setItems(R.array.mealPlan_extraOptions, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    Intent recipeIntent = new Intent(MealPlanActivity.this, RecipeActivity.class);
                                    recipeIntent.putExtra("uri",recipes.get(index).getUri());
                                    startActivity(recipeIntent);
                                }else{
                                    mRecipeViewModel.deleteRecipe(recipes.get(index));
                                    recipes.remove(index);
                                    TwoTextItemListViewAdapter itemsAdapter =  new TwoTextItemListViewAdapter(MealPlanActivity.this,
                                            R.layout.mealplan_schedule_view, recipes);
                                    mealsForDayList.setAdapter(itemsAdapter);
                                }
                            }
                        });
                builder.create().show();
                return true;
            }
        });

    }

    private void getScheduleInfo(int dayOfWeek, int date, int month, int year){
        if(recipes!=null) {
            recipes.clear();
        }
        switch (dayOfWeek){
            case 1:
                recipes = mRecipeViewModel.getRecipesByDay("Su");
                break;
            case 2:
                recipes = mRecipeViewModel.getRecipesByDay("M");
                break;
            case 3:
                recipes = mRecipeViewModel.getRecipesByDay("T");
                break;
            case 4:
                recipes = mRecipeViewModel.getRecipesByDay("W");
                break;
            case 5:
                recipes = mRecipeViewModel.getRecipesByDay("Th");
                break;
            case 6:
                recipes = mRecipeViewModel.getRecipesByDay("F");
                break;
            case 7:
                recipes = mRecipeViewModel.getRecipesByDay("S");
        }

        List<Recipe> finalRecipes = new ArrayList<>();

        if(recipes != null) {
            finalRecipes.addAll(recipes);
            for (Recipe recipe : recipes) {
                Date selectedDate = new Date(mealPlanCalender.getDate()); ;
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                if(year != 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, date);
                    selectedDate = calendar.getTime();
                }
                String selectedDateString = formatter.format(selectedDate);
                try {
                    selectedDate = formatter.parse(selectedDateString);
                } catch (ParseException e) {
                    System.err.println("Could not parse date in MealPlan");
                }
                if((recipe.getFormattedDate().after(selectedDate))
                        || (!recipe.getRepeat() && !recipe.getFormattedDate().equals(selectedDate)) ) {
                    finalRecipes.remove(recipe);
                }
            }
        }

        TwoTextItemListViewAdapter itemsAdapter =  new TwoTextItemListViewAdapter(MealPlanActivity.this,
                R.layout.mealplan_schedule_view, finalRecipes);
        recipes.clear();
        recipes.addAll(finalRecipes);
        mealsForDayList.setAdapter(itemsAdapter);
    }


    private int getDayOfWeek(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

}
