package com.ww.boengongye.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ww.boengongye.entity.DfDrawingChange;
import com.ww.boengongye.entity.DfLiableMan;
import com.ww.boengongye.entity.ImportExcelResult;
import com.ww.boengongye.mapper.DfDrawingChangeMapper;
import com.ww.boengongye.mapper.DfLiableManMapper;
import com.ww.boengongye.service.DfDrawingChangeService;
import com.ww.boengongye.service.DfFlowDataService;
import com.ww.boengongye.utils.CommunalUtils;
import com.ww.boengongye.utils.ExportExcelUtil;
import com.ww.boengongye.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 图纸变更 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-08-23
 */
@Controller
@RequestMapping("/dfDrawingChange")
@ResponseBody
@CrossOrigin
@Api(tags = "图纸变更")
public class DfDrawingChangeController {
	@Autowired
	private DfDrawingChangeService dfDrawingChangeService;

	@Autowired
	private Environment env;

	@Autowired
	private ExportExcelUtil exportExcelUtil;

	@Autowired
	private DfDrawingChangeMapper dfDrawingChangeMapper;
	@Autowired
	private DfFlowDataService dfFlowDataService;
	@Autowired
	private DfLiableManMapper dfLiableManMapper;

	private static final Logger logger = LoggerFactory.getLogger(DfQcpStandardController.class);

	@ApiOperation("型号下拉列表")
	@GetMapping("/modellList")
	public Result modellList(){
		QueryWrapper<DfDrawingChange> qw = new QueryWrapper<>();
		qw.select("model")
				.groupBy("model");
		qw.isNotNull("model");
		return new Result(200, "查询成功", dfDrawingChangeService.list(qw));
	}

	@ApiOperation("生产阶段下拉列表")
	@GetMapping("/stageList")
	public Result productionStageList(){
		QueryWrapper<DfDrawingChange> qw = new QueryWrapper<>();
		qw.select("production_stage")
				.groupBy("production_stage");
		qw.isNotNull("production_stage");
		return new Result(200, "查询成功", dfDrawingChangeService.list(qw));
	}


	@ApiOperation("分页查询")
	@GetMapping(value = "/listAll")
	public Object listAll(Integer page,Integer limit,String model,String productionStage) {
		Page<DfDrawingChange> pages = new Page<DfDrawingChange>(page, limit);
		QueryWrapper<DfDrawingChange> qw = new QueryWrapper<>();
		if (StringUtils.isNotEmpty(model)){
			qw.eq("model",model);
		}
		if (StringUtils.isNotEmpty(productionStage)){
			qw.eq("production_stage",productionStage);
		}
		qw.orderByAsc("production_stage")
				.orderByDesc("create_time");
		IPage<DfDrawingChange> list = dfDrawingChangeService.page(pages, qw);
		return new Result(0, "查询成功", list.getRecords(),(int)list.getTotal());
	}


	@ApiOperation("新增")
	@PostMapping(value = "/add")
	public Object add(DfDrawingChange dfDrawingChange) {
		if (dfDrawingChangeService.save(dfDrawingChange)) {
			return new Result(200, "保存成功");
		} else {
			return new Result(500, "保存失败");
		}
	}

	@ApiOperation("修改")
	@GetMapping(value = "/edit")
	public Object edit(DfDrawingChange dfDrawingChange) {
		if (dfDrawingChangeService.updateById(dfDrawingChange)) {
			return new Result(200, "保存成功");
		} else {
			return new Result(500, "保存失败");
		}
	}

	@ApiOperation("删除")
	@GetMapping(value = "/delete")
	public Object delete(DfDrawingChange dfDrawingChange) {
		if (dfDrawingChangeService.removeById(dfDrawingChange)) {
			return new Result(200, "删除成功");
		} else {
			return new Result(500, "删除失败");
		}
	}

	@ApiOperation("导出excel表")
	@RequestMapping(value = "/getExcel", method = { RequestMethod.GET })
	public void getExcel(String model,String productionStage, HttpServletResponse response, HttpServletRequest request) {
		try {
			QueryWrapper<DfDrawingChange> qw = new QueryWrapper<>();
			if (StringUtils.isNotEmpty(model)){
				qw.eq("model",model);
			}
			if (StringUtils.isNotEmpty(productionStage)){
				qw.eq("production_stage",productionStage);
			}
			qw.orderByAsc("model")
					.orderByAsc("production_stage")
					.orderByAsc("change_area");
			ArrayList titleKeyList = new ArrayList<>();
			titleKeyList.add("model");
			titleKeyList.add("production_stage");
			titleKeyList.add("change_area");
			titleKeyList.add("customer_drawing_name");
			titleKeyList.add("inner_drawing_name");
			titleKeyList.add("fei_drawing_name");
			titleKeyList.add("change_date");
			titleKeyList.add("flow_id");
			titleKeyList.add("category");
			titleKeyList.add("drawing_room");
			titleKeyList.add("factory_document_control");
			titleKeyList.add("outer_dfm");
			titleKeyList.add("inner_dfm");
			titleKeyList.add("bom");
			titleKeyList.add("ers");
			titleKeyList.add("size_change_list");
			titleKeyList.add("outer_qcp");
			Map titleMap = new HashMap<>();
			titleMap.put("model","型号");
			titleMap.put("production_stage","生产阶段");
			titleMap.put("change_area","变更范围");
			titleMap.put("customer_drawing_name","客户图纸名称");
			titleMap.put("inner_drawing_name","内部图纸名称");
			titleMap.put("fei_drawing_name","菲林图纸名称");
			titleMap.put("change_date","变更时间");
			titleMap.put("flow_id","流程单号");
			titleMap.put("category","类别");
			titleMap.put("drawing_room","绘图室");
			titleMap.put("factory_document_control","工厂文控");
			titleMap.put("outer_dfm","外部DFM");
			titleMap.put("inner_dfm","内部DFM");
			titleMap.put("bom","BOM");
			titleMap.put("ers","ERS");
			titleMap.put("size_change_list","尺寸变更清单");
			titleMap.put("outer_qcp","外部QCP");
			List<Map<String,Object>> datas= dfDrawingChangeService.listByExport(qw);
			exportExcelUtil.expoerDataExcel(response, titleKeyList, titleMap, datas);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@ApiOperation("Excel导入")
	@RequestMapping(value = "/uploadExcel", method = {RequestMethod.POST})
	@ResponseBody
	public Object uploadExcel(@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			Map<String, Object> map = new HashMap<>();
			System.out.println("******");
			String id2 = request.getParameter("id");// 获取data中数据
//		System.out.println(id);
			if (file != null) {
				// 获取文件名
				String fileName = file.getOriginalFilename();
				map.put("code", 0);
				System.out.println(fileName);
			} else {
				map.put("code", 1);
			}

			if (file.isEmpty()) {
				return new Result(0, "上传失败，请选择文件");
			}

//	        System.out.println(id);
//	        ImgManager img = new ImgManager();
//	        img.setArticleId(id);
			System.out.println("开始上传");
			String fileName = file.getOriginalFilename();
			System.out.println(fileName);

			if (fileName.indexOf(".xlsx") == -1 && fileName.indexOf(".xls") == -1) {
				return new Result(1, "请上传xlsx或xls格式的文件");
			}
			InputStream ins = null;
			try {
				ins = file.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File fl = new File(env.getProperty("uploadPath") + "dfDrawingChange/");
			if (!fl.exists()) {
				fl.mkdirs();
			}
			File f = new File(env.getProperty("uploadPath") + "dfDrawingChange/" + file.getOriginalFilename());
			String backupFileName = f.getName();
			int i = 1;
			while (f.exists()) {
				System.out.println(i);
				String[] list = backupFileName.split("\\.");
				fileName = list[0] + "(" + i + ")." + list[1];
				f = new File(env.getProperty("uploadPath") + "dfDrawingChange/" + fileName);
				i++;
			}

			CommunalUtils.inputStreamToFile(ins, f, env.getProperty("uploadPath"), env.getProperty("uploadPath") + "dfDrawingChange",
					env.getProperty("uploadPath") + "dfDrawingChange/");

			try {
				ImportExcelResult ter = dfDrawingChangeService.importOrder(env.getProperty("uploadPath") + "dfDrawingChange/" + fileName, file);
				return new Result(0, "上传成功", ter);
			} catch (Exception e) {

				e.printStackTrace();
			}

			return new Result(500, "上传失败");

		} catch (Exception e) {
			logger.error("导入excel接口异常", e);
		}
		return new Result(500, "接口异常");
	}

	@ApiOperation("下载模板")
	@GetMapping("/downLoadExcelMould")
	public void downLoadExcelMould(HttpServletResponse response) {
		dfDrawingChangeService.exportModel(response, "图纸标准");
	}

	@Transactional
	@ApiOperation("上传")
	@PostMapping(value = "/uploadTableAndFile")
	public Object upload(@RequestParam(value = "file", required = false) MultipartFile file , DfDrawingChange dfDrawingChange) {
		try {
			dfDrawingChange.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
			if (file.isEmpty()) {
				if (dfDrawingChangeMapper.insert(dfDrawingChange) > 0){
					return new Result(200, "上传成功");
				}
				return new Result(0, "上传失败，请选择文件");
			}
			String fileName = file.getOriginalFilename();

			InputStream ins = null;
			try {
				ins = file.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File f = CommunalUtils.generateFileByPathAndFileName(env.getProperty("uploadPath")
					,"/文件变更履历/图纸变更/文件/"
					,file.getOriginalFilename());
			String backupFileName = f.getName();
			int i = 1;
			while (f.exists()) {
				String[] list = backupFileName.split("\\.");
				fileName = list[0] + "(" + i + ")." + list[1];
				f = CommunalUtils.generateFileByPathAndFileName(env.getProperty("uploadPath")
						,"/文件变更履历/图纸变更/文件/"
						,fileName);
				i++;
			}
			dfDrawingChange.setRealPath("文件变更履历/图纸变更/文件/" + f.getName());
			int id = dfDrawingChangeMapper.insert(dfDrawingChange);
			//调PDA
			dfFlowDataService.createFlowDataFileUpdate(f.getName(),"图纸变更", id);
			//df_liable_man
			DfLiableMan dfLiableMan = new DfLiableMan();
			//添加责任人记录
//			dfLiableMan.setFactoryName("厂1")
//					.setProcessName("CNC0,CNC1")
//					.setProblemLevel("2")
//					.setDayOrNight("白班")
//					.setLiableManName("李华")
//					.setLiableManCode("admin")
//					.setType("图纸变更")
//					.setStartTime(120)
//					.setEndTime(240)
//					.setUpdateTime(LocalDateTime.now())
//					.setBimonthly("双月");
//			dfLiableManMapper.insert(dfLiableMan);
			CommunalUtils.inputStreamToFile2(ins, f);
			return new Result(200, "上传成功");
		} catch (Exception e) {
			logger.error("接口异常", e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		return new Result(500, "接口异常");
	}
}
