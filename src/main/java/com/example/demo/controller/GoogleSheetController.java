package com.example.demo.controller;

import com.example.demo.service.GoogleSheetService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GoogleSheetController {

    /**
     * Prints sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     *
     * If range is empty, return 204 No Content.
     * If range has value, return 200 OK and value.
     *
     * @param range The spreadsheet range.
     *              Example: A2:E
     * @return ResponseEntity object.
     */
    @RequestMapping(value = "/googleSheet", method = RequestMethod.GET)
    public ResponseEntity<ValueRange> googleSheet(@RequestParam(value = "range") String range)
            throws GeneralSecurityException, IOException {

        final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
        // Build a new authorized API client service.
        Sheets service = GoogleSheetService.getSheetsService();

        // Call sheet get method
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        // Return result
        if (response.getValues() == null || response.getValues().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/createSheet", method = RequestMethod.POST)
    public ResponseEntity<String> createSheet(@RequestParam(value = "title", defaultValue = "OneStopSheet") String title)
            throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        Sheets service = GoogleSheetService.getSheetsService();

        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                        .setTitle(title));
        // Call sheet create method
        spreadsheet = service.spreadsheets().create(spreadsheet)
                .setFields("spreadsheetId")
                .execute();

        // Create result object and return result
        Map<String, String> result = new HashMap<String, String>();
        result.put("Status", "Success");
        result.put("Sheet ID", spreadsheet.getSpreadsheetId());

        return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
    }

    @RequestMapping(value = "/updateSheet", method = RequestMethod.PUT)
    public ResponseEntity<String> updateSheet(@RequestParam String spreadsheetId)
            throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        Sheets service = GoogleSheetService.getSheetsService();

        // Create raw data and convert to ValueRange object
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(
                        "Row 1 Cell 1", "Row 1 Cell 2", "Row 1 Cell 3"
                )
        );
        ValueRange body = new ValueRange()
                .setValues(values);

        // Call sheet update api
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId, "A1:C1", body)
                        .setValueInputOption("RAW")
                        .execute();

        // Return result
        return new ResponseEntity<>(result.getUpdatedCells() + " cells updated.!!", HttpStatus.OK);
    }


}
