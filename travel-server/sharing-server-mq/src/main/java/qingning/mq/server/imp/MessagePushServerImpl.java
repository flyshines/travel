package qingning.mq.server.imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import qingning.common.entity.RequestEntity;
import qingning.common.util.*;
import qingning.dbcommon.mybatis.persistence.*;
import qingning.mq.server.entyity.QNSchedule;
import qingning.mq.server.entyity.ScheduleTask;
import qingning.server.AbstractMsgService;
import qingning.server.annotation.FunctionName;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.*;

public class MessagePushServerImpl extends AbstractMsgService {

    private static Logger log = LoggerFactory.getLogger(ImMsgServiceImp.class);
    @Autowired
    private CoursesMapper coursesMapper;

    @Autowired
    private CoursesStudentsMapper coursesStudentsMapper;


/*    private SaveCourseMessageService saveCourseMessageService;
    private SaveCourseAudioService saveCourseAudioService;*/

    static QNSchedule qnSchedule;
    static {
    	qnSchedule = new QNSchedule();
    }

/*    private SaveCourseMessageService getSaveCourseMessageService(ApplicationContext context){
    	if(saveCourseMessageService == null){
    		saveCourseMessageService =  (SaveCourseMessageService)context.getBean("SaveCourseMessageServer");
    	}
    	return saveCourseMessageService;
    }
    
    private SaveCourseAudioService getSaveCourseAudioService(ApplicationContext context){
    	if(saveCourseAudioService == null){
    		saveCourseAudioService =  (SaveCourseAudioService)context.getBean("SaveAudioMessageServer");
    	}
    	return saveCourseAudioService;
    }*/
    
    //课程未开播，强制结束处理定时任务
    @SuppressWarnings("unchecked")
	@FunctionName("processCourseNotStart")
    public void processForceEndCourse(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        Jedis jedis = jedisUtils.getJedis();
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        log.debug("---------------将课程加入未开播处理定时任务"+reqMap);
        String courseId = reqMap.get("course_id").toString();
        
        if(qnSchedule.containTask(courseId, QNSchedule.TASK_END_COURSE)){
        	return;
        }
        
        long startTime = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
        
        String processCourseNotStartTime = IMMsgUtil.configMap.get("course_not_start_time_msec");
        long taskStartTime = MiscUtils.convertObjectToLong(processCourseNotStartTime) + startTime;
        
        ScheduleTask scheduleTask = new ScheduleTask(){
			@Override
			public void process() {
                log.debug("-----------课程未开播处理定时任务 课程id"+this.getCourseId()+"  执行时间"+System.currentTimeMillis());
                //processCourseEnd(coursesMapper, "1", jedisUtils, courseId, jedis, getSaveCourseMessageService(context), getSaveCourseAudioService(context));
                processCourseEnd(coursesMapper, "1", jedisUtils, courseId, jedis);
			}        	
        };
        scheduleTask.setId(courseId);
        scheduleTask.setCourseId(courseId);
        scheduleTask.setStartTime(taskStartTime);
        scheduleTask.setTaskName(QNSchedule.TASK_END_COURSE);
        qnSchedule.add(scheduleTask); 
    }
    
    @SuppressWarnings("unchecked")
	@FunctionName("processLiveCourseOvertimeNotice")
    public void processLiveCourseOvertimeNotice(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        Jedis jedis = jedisUtils.getJedis();
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        log.debug("---------------将课程加入直播超时预先提醒定时任务"+reqMap);
        String courseId = reqMap.get("course_id").toString();
        String im_course_id = reqMap.get("im_course_id").toString();
        if(qnSchedule.containTask(courseId, QNSchedule.TASK_COURSE_OVER_TIME_NOTICE)){
        	return;
        }
        long real_start_time = MiscUtils.convertObjectToLong(reqMap.get("real_start_time"));
    	String courseOvertime = MiscUtils.getConfigByKey("course_live_overtime_msec");
    	long taskStartTime =MiscUtils.convertObjectToLong(courseOvertime) + real_start_time - 10*60*1000;
        if(taskStartTime>0){
        	ScheduleTask scheduleTask = new ScheduleTask(){
        		@Override
        		public void process() {
        			Map<String,Object> map = new HashMap<>();
        			map.put(Constants.CACHED_KEY_COURSE_FIELD, this.getCourseId());
        			String courseKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_COURSE, map);
        			Map<String,String> courseMap = jedis.hgetAll(courseKey);
        			if(MiscUtils.isEmpty(courseMap)  || "2".equals(courseMap.get("status"))){
        				return;
        			}
        			log.debug("-----------课程加入直播超时预先提醒定时任务 课程id"+courseId+"  执行时间"+System.currentTimeMillis());
        			JSONObject obj = new JSONObject();
        			obj.put("body",MiscUtils.getConfigByKey("jpush_course_live_overtime_per_notice"));
        			obj.put("to",courseMap.get("lecturer_id"));
        			obj.put("msg_type","4");
        			Map<String,String> extrasMap = new HashMap<>();
        			extrasMap.put("msg_type","4");
        			extrasMap.put("course_id",courseId);
        			extrasMap.put("im_course_id",im_course_id);
        			obj.put("extras_map", extrasMap);
        			JPushHelper.push(obj);                
        		}
        	};
        	scheduleTask.setId(courseId);
            scheduleTask.setCourseId(courseId);
            scheduleTask.setStartTime(taskStartTime);
            scheduleTask.setTaskName(QNSchedule.TASK_COURSE_OVER_TIME_NOTICE);
            qnSchedule.add(scheduleTask); 
        }
    }
    
    @FunctionName("processCourseStartLongNotice")
    public void processCourseStartLongNotice(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        @SuppressWarnings("unchecked")
		Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        log.debug("---------------将课程加入开播预先24H提醒定时任务"+reqMap);
        String courseId = reqMap.get("course_id").toString();
        if(qnSchedule.containTask(courseId, QNSchedule.TASK_COURSE_24H_NOTICE)){
        	return;
        }
        String lecturer_id = reqMap.get("lecturer_id").toString();
        String course_title = reqMap.get("course_title").toString();
        String im_course_id = reqMap.get("im_course_id").toString();
        long start_time = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
        long noticeTime= 24*60*60*1000;
        long taskStartTime = start_time - noticeTime;
        if(taskStartTime>0){
        	ScheduleTask scheduleTask = new ScheduleTask(){
        		@Override
        		public void process() {
                    log.debug("-----------开播预先24H提醒定时任务 课程id"+courseId+"  执行时间"+System.currentTimeMillis());
                    String fotmatString = "HH:mm";
                    String startTimeFormat = MiscUtils.parseDateToFotmatString((Date)(reqMap.get("start_time")),fotmatString);
                    JSONObject obj = new JSONObject();
                    obj.put("body",String.format(MiscUtils.getConfigByKey("jpush_course_start_per_long_notice"), MiscUtils.RecoveryEmoji(course_title),startTimeFormat));
                    obj.put("to",lecturer_id);
                    obj.put("msg_type","1");
                    Map<String,String> extrasMap = new HashMap<>();
                    extrasMap.put("msg_type","1");
                    extrasMap.put("course_id",courseId);
                    extrasMap.put("im_course_id",im_course_id);
                    obj.put("extras_map", extrasMap);
                    JPushHelper.push(obj);               
        		}
        	};
        	scheduleTask.setId(courseId);
            scheduleTask.setCourseId(courseId);
            scheduleTask.setStartTime(taskStartTime);
            scheduleTask.setTaskName(QNSchedule.TASK_COURSE_24H_NOTICE);
            qnSchedule.add(scheduleTask); 
        }
    }
    
    @FunctionName("processCourseStartShortNotice")
    public void processCourseStartShortNotice(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        @SuppressWarnings("unchecked")
		Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        log.debug("---------------将课程加入开播预先5min提醒定时任务"+reqMap);
        String courseId = reqMap.get("course_id").toString();
        if(qnSchedule.containTask(courseId, QNSchedule.TASK_COURSE_5MIN_NOTICE)){
        	return;
        }
        String lecturer_id = reqMap.get("lecturer_id").toString();
        String course_title = reqMap.get("course_title").toString();
        String im_course_id = reqMap.get("im_course_id").toString();
        long start_time = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
        long noticeTime= 5*60*1000;
        long taskStartTime = start_time - noticeTime;
        if(taskStartTime>0){
        	ScheduleTask scheduleTask = new ScheduleTask(){
        		@Override
        		public void process() {
        			log.debug("-----------开播预先5min提醒定时任务 课程id"+courseId+"  执行时间"+System.currentTimeMillis());
                    JSONObject obj = new JSONObject();
                    obj.put("body",String.format(MiscUtils.getConfigByKey("jpush_course_start_per_short_notice"), MiscUtils.RecoveryEmoji(course_title),"5"));
                    obj.put("to",lecturer_id);
                    obj.put("msg_type","2");
                    Map<String,String> extrasMap = new HashMap<>();
                    extrasMap.put("msg_type","2");
                    extrasMap.put("course_id",courseId);
                    extrasMap.put("im_course_id",im_course_id);
                    obj.put("extras_map", extrasMap);
                    JPushHelper.push(obj);
        		}
        	};
        	scheduleTask.setId(courseId);
            scheduleTask.setCourseId(courseId);
            scheduleTask.setStartTime(taskStartTime);
            scheduleTask.setTaskName(QNSchedule.TASK_COURSE_5MIN_NOTICE);
            qnSchedule.add(scheduleTask); 
        }
    }
    
    
    @SuppressWarnings("unchecked")
	@FunctionName("processCourseStartStudentStudyNotice")
    public void processCourseStartStudentStudyNotice(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        log.debug("---------------加入学生上课3min提醒定时任务"+reqMap);
        String courseId = reqMap.get("course_id").toString();
        String im_course_id = reqMap.get("im_course_id").toString();
        if(qnSchedule.containTask(courseId, QNSchedule.TASK_COURSE_15MIN_NOTICE)){
        	return;
        }
        String course_title = reqMap.get("course_title").toString();
        long start_time = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
        long noticeTime= 3*60*1000;
        long taskStartTime = start_time - noticeTime;
        if(taskStartTime > 0){
        	ScheduleTask scheduleTask = new ScheduleTask(){
        		@Override
        		public void process() {
                    log.debug("-----------加入学生上课3min提醒定时任务 课程id"+courseId+"  执行时间"+System.currentTimeMillis());
                    JSONObject obj = new JSONObject();
                    obj.put("body",String.format(MiscUtils.getConfigByKey("jpush_course_study_notice"), MiscUtils.RecoveryEmoji(course_title)));
                    List<String> studentIds = coursesStudentsMapper.findUserIdsByCourseId(courseId);
                    obj.put("user_ids",studentIds);
                    obj.put("msg_type","10");
                    Map<String,String> extrasMap = new HashMap<>();
                    extrasMap.put("msg_type","10");
                    extrasMap.put("course_id",courseId);
                    extrasMap.put("im_course_id", im_course_id);
                    obj.put("extras_map", extrasMap);
                    JPushHelper.push(obj);
        		}
        	};
        	scheduleTask.setId(courseId);
            scheduleTask.setCourseId(courseId);
            scheduleTask.setStartTime(taskStartTime);
            scheduleTask.setTaskName(QNSchedule.TASK_COURSE_15MIN_NOTICE);
            qnSchedule.add(scheduleTask); 
        }
    }
    
    @SuppressWarnings("unchecked")
	@FunctionName("processCourseStartLecturerNotShow")
    public void processCourseStartLecturerNotShow(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        Jedis jedis = jedisUtils.getJedis();
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        log.debug("---------------将课程加入直播开始但是讲师未出现提醒定时任务"+reqMap);
        String courseId = reqMap.get("course_id").toString();
        if(qnSchedule.containTask(courseId, QNSchedule.TASK_LECTURER_NOTICE)){
        	return;
        }
        String lecturer_id = reqMap.get("lecturer_id").toString();
        String course_title = reqMap.get("course_title").toString();
        String im_course_id = reqMap.get("im_course_id").toString();
        long taskStartTime = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
        if(taskStartTime > 0){
        	ScheduleTask scheduleTask = new ScheduleTask(){
        		@Override
        		public void process() {
                    log.debug("-----------课程加入直播开始但是讲师未出现提醒定时任务 课程id"+courseId+"  执行时间"+System.currentTimeMillis());
                    Map<String,Object> courseCacheMap = new HashMap<>();
                    courseCacheMap.put(Constants.CACHED_KEY_COURSE_FIELD, courseId);
                    String courseKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_COURSE, courseCacheMap);
                    Map<String,String> courseMap = jedis.hgetAll(courseKey);
                    //如果课程不是预告中，则不需要执行该定时任务
                    if(courseMap == null || courseMap.size() == 0 || !courseMap.get("status").equals("1")){                       
                        return;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("body",String.format(MiscUtils.getConfigByKey("jpush_course_start_lecturer_not_show"), MiscUtils.RecoveryEmoji(course_title)));
                    obj.put("to",lecturer_id);
                    obj.put("msg_type","3");
                    Map<String,String> extrasMap = new HashMap<>();
                    extrasMap.put("msg_type","3");
                    extrasMap.put("course_id",courseId);
                    extrasMap.put("im_course_id",im_course_id);
                    obj.put("extras_map", extrasMap);
                    JPushHelper.push(obj);
        		}
        	};
        	scheduleTask.setId(courseId);
            scheduleTask.setCourseId(courseId);
            scheduleTask.setLecturerId(lecturer_id);
            scheduleTask.setStartTime(taskStartTime);
            scheduleTask.setTaskName(QNSchedule.TASK_LECTURER_NOTICE);
            qnSchedule.add(scheduleTask); 
        }
    } 
    
    //课程直播超时处理
    @SuppressWarnings("unchecked")
	@FunctionName("processCourseLiveOvertime")
    public void processCourseLiveOvertime(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context){
        Jedis jedis = jedisUtils.getJedis();
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        log.debug("---------------将课程加入直播超时处理定时任务"+reqMap);
        String courseId = reqMap.get("course_id").toString();
        if(qnSchedule.containTask(courseId, QNSchedule.TASK_LECTURER_NOTICE)){
        	return;
        }
        long realStartTime = MiscUtils.convertObjectToLong(reqMap.get("real_start_time"));
        long courseLiveOvertimeMsec = MiscUtils.convertObjectToLong(IMMsgUtil.configMap.get("course_live_overtime_msec"));
        long taskStartTime = courseLiveOvertimeMsec + realStartTime;
        
        if(taskStartTime>0){
        	ScheduleTask scheduleTask = new ScheduleTask(){
        		@Override
        		public void process() {
                    log.debug("课程直播超时处理定时任务 课程id"+courseId+"  执行时间"+System.currentTimeMillis());
                    processCourseEnd(coursesMapper,"2",jedisUtils,courseId,jedis);
        		}
        	};
        	scheduleTask.setId(courseId);
            scheduleTask.setCourseId(courseId);
            scheduleTask.setStartTime(taskStartTime);
            scheduleTask.setTaskName(QNSchedule.TASK_LECTURER_NOTICE);
            qnSchedule.add(scheduleTask);
        }
    }
    
    //开播预先24H提醒定时任务取消
    @SuppressWarnings("unchecked")
	@FunctionName("processCourseStartLongNoticeCancel")
    public void processCourseStartLongNoticeCancel(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        String courseId = reqMap.get("course_id").toString();        
        qnSchedule.cancelTask(courseId, QNSchedule.TASK_COURSE_24H_NOTICE);
    }

    //课程未开播处理定时任务取消
    @SuppressWarnings("unchecked")
	@FunctionName("processCourseNotStartCancel")
    public void processCourseNotStartCancel(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
        String courseId = reqMap.get("course_id").toString();
        qnSchedule.cancelTask(courseId, QNSchedule.TASK_END_COURSE);
    }

    //课程未开播处理定时任务取消
    @SuppressWarnings("unchecked")
	@FunctionName("processCourseNotStartUpdate")
    public void processCourseNotStartUpdate(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
        processCourseNotStartCancel(requestEntity,jedisUtils,context);
        
    	Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
    	long startTime = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
    	if(MiscUtils.isTheSameDate(new Date(startTime), new Date())){
    		processForceEndCourse(requestEntity,jedisUtils,context);
    	}
    }
    
    
	@FunctionName("processCourseStartShortNoticeUpdate")
    public void processCourseStartShortNoticeUpdate(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
    	processCourseStartShortNoticeUpdate(requestEntity, jedisUtils, context, true);
    }
	@FunctionName("processCourseStartShortNoticeCancel")
    public void processCourseStartShortNoticeCancel(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
    	processCourseStartShortNoticeUpdate(requestEntity, jedisUtils, context, false);
    }
	
	@SuppressWarnings("unchecked")
    private void processCourseStartShortNoticeUpdate(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context, boolean update) {	
    	Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
    	long startTime = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
    	String courseId = (String)reqMap.get("course_id");
    	qnSchedule.cancelTask(courseId, QNSchedule.TASK_COURSE_5MIN_NOTICE);    	
    	if(update && MiscUtils.isTheSameDate(new Date(startTime), new Date())){
    		if(startTime-System.currentTimeMillis()> 5 * 60 *1000){
    			this.processCourseStartShortNotice(requestEntity, jedisUtils, context);
    		}
    	}
    }
    
    @SuppressWarnings("unchecked")
	@FunctionName("processCourseStartLongNoticeUpdate")
    public void processCourseStartLongNoticeUpdate(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
    	Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
    	long startTime = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
    	String courseId = (String)reqMap.get("course_id");
    	qnSchedule.cancelTask(courseId, QNSchedule.TASK_COURSE_24H_NOTICE);
    	
    	if(MiscUtils.isTheSameDate(new Date(startTime- 60 * 60 *1000*24), new Date()) && startTime-System.currentTimeMillis()> 60 * 60 *1000*24){
    		processCourseStartLongNotice(requestEntity, jedisUtils, context);
    	}
    }
    @SuppressWarnings("unchecked")
    @FunctionName("processCourseStartStudentStudyNoticeUpdate")
    public void processCourseStartStudentStudyNoticeUpdate(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
    	Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
    	long startTime = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
    	String courseId = (String)reqMap.get("course_id");
    	qnSchedule.cancelTask(courseId, QNSchedule.TASK_COURSE_15MIN_NOTICE);
    	
    	if(MiscUtils.isTheSameDate(new Date(startTime), new Date())){
    		if(startTime-System.currentTimeMillis()> 5 * 60 *1000){
    			processCourseStartStudentStudyNotice(requestEntity, jedisUtils, context);
    		}
    	}
    }
    
    @SuppressWarnings("unchecked")
    @FunctionName("processCourseStartLecturerNotShowUpdate")
    public void processCourseStartLecturerNotShowUpdate(RequestEntity requestEntity, JedisUtils jedisUtils, ApplicationContext context) {
    	Map<String, Object> reqMap = (Map<String, Object>) requestEntity.getParam();
    	long startTime = MiscUtils.convertObjectToLong(reqMap.get("start_time"));
    	String courseId = (String)reqMap.get("course_id");
    	qnSchedule.cancelTask(courseId, QNSchedule.TASK_LECTURER_NOTICE);
    	
    	if(MiscUtils.isTheSameDate(new Date(startTime), new Date())){
    		if(startTime > System.currentTimeMillis()){
    			processCourseStartLecturerNotShow(requestEntity, jedisUtils, context);
    		}
    	}
    }
    
    //type 为1则为课程未开播强制结束，type为2则为课程直播超时强制结束
    private void processCourseEnd(CoursesMapper processCoursesMapper, String type ,JedisUtils jedisUtils, String courseId, Jedis jedis){
        Map<String,Object> map = new HashMap<>();
        map.put(Constants.CACHED_KEY_COURSE_FIELD, courseId);
        String courseKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_COURSE, map);
        Map<String,String> courseMap = jedis.hgetAll(courseKey);
        long realStartTime = MiscUtils.convertObjectToLong(courseMap.get("real_start_time"));

        boolean processFlag = false;
        if(type.equals("1") && realStartTime < 1){
        	processFlag = true;
        }else if(type.equals("2")){
            if(! courseMap.get("status").equals("2")){
                processFlag = true;
            }
        }

        //如果为未开播课程，则对课程进行结束处理
        if(processFlag){
            //1.1如果为课程结束，则取当前时间为课程结束时间
            Date now = new Date();
            //1.2更新课程详细信息（变更课程为已经结束）
            Map<String,Object> course = new HashMap<String,Object>();
            course.put("course_id", courseId);
            course.put("end_time", now);
            course.put("update_time", now);
            course.put("status", "2");           
            processCoursesMapper.updateCourse(course);

            //1.3将该课程从讲师的预告课程列表 SYS: lecturer:{ lecturer_id }：courses  ：prediction移动到结束课程列表 SYS: lecturer:{ lecturer_id }：courses  ：finish
            map.clear();
            map.put(Constants.CACHED_KEY_LECTURER_FIELD, courseMap.get("lecturer_id"));
            String lecturerCoursesPredictionKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_COURSE_PREDICTION, map);
            jedis.zrem(lecturerCoursesPredictionKey, courseId);

            String lecturerCoursesFinishKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_COURSE_FINISH, map);
            String courseStartTime = jedis.hget(courseKey, "start_time");
            
            long  position = MiscUtils.convertObjectToLong(jedis.hget(courseKey, "position"));
            jedis.zadd(lecturerCoursesFinishKey, MiscUtils.convertInfoToPostion(now.getTime(), position), courseId);

            //1.4将该课程从平台的预告课程列表 SYS：courses  ：prediction移除。如果存在结束课程列表 SYS：courses ：finish，则增加到课程结束列表
            jedis.zrem(Constants.CACHED_KEY_PLATFORM_COURSE_PREDICTION, courseId);
            if(jedis.exists(Constants.CACHED_KEY_PLATFORM_COURSE_FINISH)){            	
                jedis.zadd(Constants.CACHED_KEY_PLATFORM_COURSE_FINISH, MiscUtils.convertInfoToPostion( now.getTime(),position), courseId);
            }

            //1.5如果课程标记为结束，则清除该课程的禁言缓存数据
            map.put(Constants.CACHED_KEY_COURSE_FIELD, courseMap.get("lecturer_id"));
            String banKey = MiscUtils.getKeyOfCachedData(Constants.CACHED_KEY_COURSE_BAN_USER_LIST, map);
            jedis.del(banKey);

            //1.6更新课程缓存信息
            Map<String, String> updateCacheMap = new HashMap<String, String>();
            updateCacheMap.put("update_time", MiscUtils.convertObjectToLong(course.get("end_time")) + "");
            updateCacheMap.put("end_time", MiscUtils.convertObjectToLong(course.get("end_time")) + "");
            updateCacheMap.put("status", "2");
            jedis.hmset(courseKey, updateCacheMap);

            ////发送结束推送消息
            SimpleDateFormat sdf =   new SimpleDateFormat("yyyy年MM月dd日HH:mm");
            String str = sdf.format(now);
            String courseEndMessage = "直播结束于"+str;
            long currentTime = System.currentTimeMillis();
            String mGroupId = jedis.hget(courseKey,"im_course_id");
            String message = courseEndMessage;
            String sender = "system";
            Map<String,Object> infomation = new HashMap<>();
            infomation.put("course_id", courseId);
            infomation.put("creator_id", courseMap.get("lecturer_id"));
            infomation.put("message", message);
            infomation.put("message_type", "1");
            infomation.put("send_type", "6");//5.结束消息
            infomation.put("create_time", currentTime);
            Map<String,Object> messageMap = new HashMap<>();
            messageMap.put("msg_type","1");
            messageMap.put("send_time",currentTime);
            messageMap.put("information",infomation);
            messageMap.put("mid",MiscUtils.getUUId());
            String content = JSON.toJSONString(messageMap);
            IMMsgUtil.sendMessageInIM(mGroupId, content, "", sender);

/*            //1.7如果存在课程聊天信息
            RequestEntity messageRequestEntity = new RequestEntity();
            Map<String,Object> processMap = new HashMap<>();
            processMap.put("course_id", courseId);
            messageRequestEntity.setParam(processMap);
            try {
                saveCourseMessageService.process(messageRequestEntity, jedisUtils, null);
            } catch (Exception e) {
                //TODO 暂时不处理
            }

            //1.8如果存在课程音频信息
            RequestEntity audioRequestEntity = new RequestEntity();
            audioRequestEntity.setParam(processMap);
            try {
                saveCourseAudioService.process(audioRequestEntity, jedisUtils, null);
            } catch (Exception e) {
                //TODO 暂时不处理
            }*/

            //课程未开播强制结束
            if(type.equals("1")){
                //1.9极光推送结束消息
                JSONObject obj = new JSONObject();
                obj.put("body",String.format(MiscUtils.getConfigByKey("jpush_course_not_start_force_end"),MiscUtils.RecoveryEmoji(courseMap.get("course_title"))));
                obj.put("to",courseMap.get("lecturer_id"));
                obj.put("msg_type","6");
                Map<String,String> extrasMap = new HashMap<>();
                extrasMap.put("msg_type","6");
                extrasMap.put("course_id",courseId);
                extrasMap.put("im_course_id",courseMap.get("im_course_id"));
                obj.put("extras_map", extrasMap);
                JPushHelper.push(obj);
            }else if(type.equals("2")){
                //课程直播超时结束
                //1.9极光推送结束消息
                JSONObject obj = new JSONObject();
                obj.put("body",String.format(MiscUtils.getConfigByKey("jpush_course_live_overtime_force_end"),MiscUtils.RecoveryEmoji(courseMap.get("course_title"))));
                obj.put("to",courseMap.get("lecturer_id"));
                obj.put("msg_type","5");
                Map<String,String> extrasMap = new HashMap<>();
                extrasMap.put("msg_type","5");
                extrasMap.put("course_id",courseId);
                extrasMap.put("im_course_id",courseMap.get("im_course_id"));
                obj.put("extras_map", extrasMap);
                JPushHelper.push(obj);
            }


        }
    }

    public CoursesMapper getCoursesMapper() {
        return coursesMapper;
    }

    public void setCoursesMapper(CoursesMapper coursesMapper) {
        this.coursesMapper = coursesMapper;
    }

/*    public SaveCourseMessageService getSaveCourseMessageService() {
        return saveCourseMessageService;
    }

    public void setSaveCourseMessageService(SaveCourseMessageService saveCourseMessageService) {
        this.saveCourseMessageService = saveCourseMessageService;
    }

    public SaveCourseAudioService getSaveCourseAudioService() {
        return saveCourseAudioService;
    }

    public void setSaveCourseAudioService(SaveCourseAudioService saveCourseAudioService) {
        this.saveCourseAudioService = saveCourseAudioService;
    }*/
}
