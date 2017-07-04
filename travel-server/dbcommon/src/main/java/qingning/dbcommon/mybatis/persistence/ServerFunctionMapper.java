package qingning.dbcommon.mybatis.persistence;

import java.util.List;
import java.util.Map;

public interface ServerFunctionMapper {
    List<Map<String,Object>> getServerUrls();
}