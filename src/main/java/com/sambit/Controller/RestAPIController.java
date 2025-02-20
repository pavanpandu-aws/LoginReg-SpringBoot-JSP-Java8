package com.sambit.Controller;

import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.sambit.Entity.Reg;
import com.sambit.Service.RegService;
import org.apache.catalina.LifecycleState;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.sax.SAXResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Project : Registration
 * @Auther : Sambit Kumar Pradhan
 * @Created On : 29/09/2022 - 9:57 AM
 */
@RestController
@RequestMapping(value = "/api")
public class RestAPIController {

	@Autowired
	private RegService regService;

	@GetMapping(value = "/checkUserIsPresent")
	public Map<String, Object> checkUserIsPresent(@RequestParam(value = "userName") String userName){
		Map<String, Object> map = new HashMap<>();
		try {
			boolean isPresent = regService.checkUserIsPresentOrNot(userName);
			if(isPresent){
				map.put("status", HttpStatus.OK.value());
				map.put("message", "User is Present");
			}else {
				map.put("status", HttpStatus.NOT_FOUND.value());
				map.put("message", "User is Not Present");
			}
		} catch (Exception e) {
			map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put("message", "Something Went Wrong");
		}
		return map;
	}

	@PostMapping(value = "/registerUser")
	public Map<String, Object> registerUser(@RequestBody Reg reg){
		Map<String, Object> map = new HashMap<>();
		try {
			Reg saveReg = regService.saveReg(reg);
			if (saveReg != null && saveReg.getSlno() > 0) {
				map.put("status", HttpStatus.OK.value());
				map.put("message", "User Registered Successfully");
			} else {
				map.put("status", HttpStatus.NOT_FOUND.value());
				map.put("message", "User Registration Failed");
			}
		}catch (Exception e) {
			map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put("message", "Something Went Wrong");
		}
		return map;
	}

	@DeleteMapping(value = "/deleteUser")
	public Map<String, Object> deleteUser(@RequestParam(value = "slno") int slno){
		System.out.println("slno = " + slno);
		Map<String, Object> map = new HashMap<>();
		try {
			boolean isPresent = regService.checkRegIsPresentOrNotBySlNo(slno);
			if (isPresent) {
				boolean isDeleted = regService.deleteReg(slno);
				if (isDeleted) {
					map.put("status", HttpStatus.OK.value());
					map.put("message", "User Deleted Successfully");
				} else {
					map.put("status", HttpStatus.NOT_FOUND.value());
					map.put("message", "User Deletion Failed");
				}
			}else {
				map.put("status", HttpStatus.NOT_FOUND.value());
				map.put("message", "User is Not Present");
			}
		} catch (Exception e) {
			map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put("message", "Something Went Wrong");
			throw new RuntimeException(e);
		}
		return map;
	}

	@PutMapping(value = "/updateUser")
	public Map<String, Object> updateUser(@RequestBody Reg reg){
		Map<String, Object> map = new HashMap<>();
		try {
			boolean isPresent = regService.checkRegIsPresentOrNotBySlNo(reg.getSlno());
			if (isPresent) {
				Reg saveReg = regService.saveReg(reg);
				if (saveReg != null && saveReg.getSlno() > 0) {
					map.put("status", HttpStatus.OK.value());
					map.put("message", "User Updated Successfully");
				} else {
					map.put("status", HttpStatus.NOT_FOUND.value());
					map.put("message", "User Updation Failed");
				}
			}else {
				map.put("status", HttpStatus.NOT_FOUND.value());
				map.put("message", "User is Not Present");
			}
		} catch (Exception e) {
			map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put("message", "Something Went Wrong");
			throw new RuntimeException(e);
		}
		return map;
	}

	@GetMapping(value = "/getUserDetails")
	public Map<String, Object> getUserDetails(@RequestParam(value = "slno") int slno){
		Map<String, Object> map = new HashMap<>();
		try {
			boolean isPresent = regService.checkRegIsPresentOrNotBySlNo(slno);
			if (isPresent) {
				Reg reg = regService.getRegBySlNo(slno);
				if (reg != null) {
					map.put("status", HttpStatus.OK.value());
					map.put("message", "User Details Found");
					map.put("data", reg.toString());
				} else {
					map.put("status", HttpStatus.NOT_FOUND.value());
					map.put("message", "User Details Not Found");
				}
			}
		} catch (Exception e) {
			map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put("message", "Something Went Wrong");
			throw new RuntimeException(e);
		}
		return map;
	}

	@GetMapping(value = "/getAllUserDetails")
	public Map<String, Object> getAllUserDetails(){
		Map<String, Object> map = new HashMap<>();
		try {
			List<Reg> regList = regService.getAllReg();
			if (regList != null && regList.size() > 0) {
				map.put("status", HttpStatus.OK.value());
				map.put("message", "User Details Found");
				map.put("data", regList.toString());
			} else {
				map.put("status", HttpStatus.NOT_FOUND.value());
				map.put("message", "User Details Not Found");
			}
		} catch (Exception e) {
			map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put("message", "Something Went Wrong");
			throw new RuntimeException(e);
		}
		return map;
	}

	@GetMapping(value = "/getAllUserDetailsByJSON")
	public String getAllUsersData() throws JSONException {
		JSONObject mainJsonObj = new JSONObject();
		JSONArray jsonArray;
		JSONObject jsonObj;
		try {
			List<Reg> regList = regService.getAllReg();
			if (regList != null && regList.size() > 0){
				jsonArray = new JSONArray();
				for (Reg reg : regList) {
					jsonObj = new JSONObject();
					jsonObj.put("slno", reg.getSlno());
					jsonObj.put("name", reg.getName());
					jsonObj.put("email", reg.getEmail());
					jsonObj.put("mobile", reg.getPhn());
					jsonObj.put("gender", reg.getGender());
					jsonObj.put("department", reg.getDept());
					jsonObj.put("usercode", reg.getUserCode());
					jsonObj.put("password", reg.getPassword());
					jsonArray.put(jsonObj);
				}
				mainJsonObj.put("status", HttpStatus.OK.value());
				mainJsonObj.put("message", "User Details list Found");
				mainJsonObj.put("data", jsonArray);
			}else {
				mainJsonObj.put("status", HttpStatus.NOT_FOUND.value());
				mainJsonObj.put("message", "User Details List Not Found");
			}
		}catch (Exception e) {
			mainJsonObj.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			mainJsonObj.put("message", "Something Went Wrong");
			throw new RuntimeException(e);
		}
		return mainJsonObj.toString();
	}

	@GetMapping(value = "/getPaaswordByUsercode")
	public String getPasswordByUsercode(@RequestParam(value = "usercode") String usercode) throws JSONException {
		JSONObject mainJsonObj = new JSONObject();
		try {
			boolean isPresent = regService.checkRegIsPresentOrNotByUserCode(usercode);
			if (isPresent) {
				Reg reg = regService.getRegByUserCode(usercode);
				if (reg != null) {
					mainJsonObj.put("status", HttpStatus.OK.value());
					mainJsonObj.put("message", "User Details Found");
					mainJsonObj.put("data", reg.getPassword());
				} else {
					mainJsonObj.put("status", HttpStatus.NOT_FOUND.value());
					mainJsonObj.put("message", "User Details Not Found");
				}
			}
		} catch (Exception e) {
			mainJsonObj.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			mainJsonObj.put("message", "Something Went Wrong");
			throw new RuntimeException(e);
		}
		return mainJsonObj.toString();
	}
}
