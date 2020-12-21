package com.qrapids.backlog_gitlab.services;


import com.qrapids.backlog_gitlab.data.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
public class BacklogServices {

    @Value("${gitlab.url}")
    private String gitlabURL;

    @Value("${gitlab.secret}")
    private String token;

    @GetMapping("/api/milestones")
    public ResponseEntity<Object> getMilestones(@RequestParam String project_id,
                                                @RequestParam(value = "date_from", required = false) String date_from) {
        try {
            // Creating a Request with authentication by token
            URL url = new URL(gitlabURL + "/api/v4/projects/" + project_id + "/milestones?private_token="+token);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            // Setting Request Headers
            con.setRequestProperty("Content-Type", "application/json");
            // Configuring Timeouts
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            // Reading the Response
            int status = con.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                JsonParser parser = new JsonParser();
                JsonArray data = parser.parse(content.toString()).getAsJsonArray();
                List<Milestone> milestones = new ArrayList<>();
                for (int i = 0; i < data.size(); ++i) {
                    JsonObject object = data.get(i).getAsJsonObject();
                    if (!object.get("due_date").isJsonNull()) { // check if milestone have due_date
                        String date = object.get("due_date").getAsString();
                        if(date_from != null && !date_from.isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date from = sdf.parse(date_from);
                            Date due = sdf.parse(date);
                            if (due.equals(from) || due.after(from)) { // only add milestones which will finish after date_from
                                Milestone newMilestone = new Milestone();
                                newMilestone.setName(object.get("title").getAsString());
                                newMilestone.setDate(date);
                                newMilestone.setDescription(object.get("description").getAsString());
                                newMilestone.setType("Milestone");
                                milestones.add(newMilestone);
                            }
                        } else { // if there is no date_from specified --> we add all milestones with due_date
                            Milestone newMilestone = new Milestone();
                            newMilestone.setName(object.get("title").getAsString());
                            newMilestone.setDate(date);
                            newMilestone.setDescription(object.get("description").getAsString());
                            newMilestone.setType("Milestone");
                            milestones.add(newMilestone);
                        }
                    }
                }
                Collections.sort(milestones, (Milestone o1, Milestone o2) -> o1.getDate().compareTo(o2.getDate()));
                return new ResponseEntity<>(milestones, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*@GetMapping("/api/phases")
    public ResponseEntity<Object> getPhases(@RequestParam String project_id,
                                                @RequestParam(value = "date_from", required = false) String date_from) throws ParseException {
        ResponseEntity<Object> milestonesList = getMilestones(project_id,date_from);
        if (milestonesList.getStatusCode() == HttpStatus.OK) {
            List<Milestone> milestones = (List<Milestone>) milestonesList.getBody();
            List<Phase> phases = new ArrayList<>();
            if (milestones.isEmpty())
                return new ResponseEntity<>(phases, HttpStatus.OK);
            else {
                // get next milestone from today
                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                int i = 0;
                boolean found = false;
                while (i < milestones.size() && !found) {
                    Date due = sdf.parse(milestones.get(i).getDate());
                    if (due.after(now))
                        found = true;
                    else
                        i++;
                }
                if (found) {
                    // put milestone phase to the list
                    Phase firstPhase = new Phase();
                    LocalDate date = LocalDate.parse(milestones.get(i).getDate()); // milestone date
                    firstPhase.setDateFrom(date.minusWeeks(1).toString());
                    firstPhase.setDateTo(date.toString());
                    firstPhase.setName("");
                    firstPhase.setDescription("");
                    phases.add(firstPhase);
                    // add others phases to the list
                    for (int j = 1; j < 10; ++j) {
                        Phase newPhase = new Phase();
                        newPhase.setDateFrom(date.minusWeeks(j + 1).toString());
                        newPhase.setDateTo(date.minusWeeks(j).toString());
                        newPhase.setName("");
                        newPhase.setDescription("");
                        phases.add(newPhase);
                    }
                }
                return new ResponseEntity<>(phases, HttpStatus.OK);
            }
        } else {
            ErrorResponse error = (ErrorResponse) milestonesList.getBody();
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    */

    @GetMapping("/api/phases")
    public ResponseEntity<Object> getPhases(@RequestParam String project_id,
                                            @RequestParam(value = "date_from", required = false) String date_from) throws ParseException {
        LocalDate date = LocalDate.now();
        List<Phase> phases = new ArrayList<>();
        // add others phases to the list
        for (int j = 0; j < 5; ++j) {
            Phase newPhase = new Phase();
            newPhase.setDateFrom(date.minusWeeks(2).toString());
            newPhase.setDateTo(date.toString());
            newPhase.setName("");
            newPhase.setDescription("");
            phases.add(newPhase);
            date = date.minusWeeks(2);
        }
        return new ResponseEntity<>(phases, HttpStatus.OK);
    }

    @PostMapping("/api/createIssue")
    public ResponseEntity<Object> createIssue(@RequestBody QualityRequirement requirement) {
        try {
            // Creating a Request with authentication by token
            String url_str = gitlabURL + "/api/v4/projects/" + requirement.getProject_id() + "/issues?private_token="+token;
            URL url = new URL(url_str);
            // Make params map
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("title", requirement.getIssue_summary());
            params.put("description", requirement.getIssue_description() + "\n" + requirement.getDecision_rationale());
            // Create the Request Body
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            // Setting Request Headers
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            // Reading the Response
            int status = conn.getResponseCode();
            System.out.println(status);
            if (status != 201) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {

                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());

                    JsonParser parser = new JsonParser();
                    JsonObject object = parser.parse(response.toString()).getAsJsonObject();
                    System.out.println(object);
                    SuccessResponse newIssue = new SuccessResponse(object.get("iid").getAsString(), object.get("web_url").getAsString());
                    return new ResponseEntity<>(newIssue, HttpStatus.OK);
                }
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
