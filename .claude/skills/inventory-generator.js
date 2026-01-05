/**
 * 进销存系统代码生成器
 *
 * 根据业务需求生成进销存相关的代码骨架和模板
 */

/**
 * 生成 Vue 3 商品列表页面模板
 * @param {string} moduleName - 模块名称 (product/inventory/inbound/outbound)
 * @returns {string} Vue 组件代码
 */
function generateInventoryListPage(moduleName) {
  const moduleConfig = {
    product: {
      name: '商品',
      nameEn: 'Product',
      apiPath: 'product',
      columns: [
        { prop: 'sku', label: '商品编码', width: 120 },
        { prop: 'name', label: '商品名称' },
        { prop: 'categoryName', label: '分类', width: 100 },
        { prop: 'price', label: '价格', width: 100, format: 'price' },
        { prop: 'stock', label: '库存', width: 100, format: 'stock' }
      ]
    },
    inventory: {
      name: '库存',
      nameEn: 'Inventory',
      apiPath: 'inventory',
      columns: [
        { prop: 'productSku', label: '商品编码', width: 120 },
        { prop: 'productName', label: '商品名称' },
        { prop: 'quantity', label: '库存数量', width: 120 },
        { prop: 'warehouseName', label: '仓库', width: 100 }
      ]
    },
    inbound: {
      name: '入库',
      nameEn: 'Inbound',
      apiPath: 'inbound',
      columns: [
        { prop: 'inboundNo', label: '入库单号', width: 150 },
        { prop: 'productName', label: '商品名称' },
        { prop: 'quantity', label: '入库数量', width: 100 },
        { prop: 'supplier', label: '供应商', width: 150 },
        { prop: 'inboundDate', label: '入库日期', width: 120 }
      ]
    },
    outbound: {
      name: '出库',
      nameEn: 'Outbound',
      apiPath: 'outbound',
      columns: [
        { prop: 'outboundNo', label: '出库单号', width: 150 },
        { prop: 'productName', label: '商品名称' },
        { prop: 'quantity', label: '出库数量', width: 100 },
        { prop: 'receiver', label: '收货人', width: 150 },
        { prop: 'outboundDate', label: '出库日期', width: 120 }
      ]
    }
  };

  const config = moduleConfig[moduleName];
  if (!config) {
    throw new Error(`Unknown module: ${moduleName}`);
  }

  return `<template>
  <div class="${config.apiPath}-list">
    <!-- 搜索区域 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      ${config.columns.slice(0, 3).map(col => `
      <el-form-item label="${col.label}">
        <el-input v-model="searchForm.${col.prop}" placeholder="请输入${col.label}" />
      </el-form-item>`).join('')}
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增${config.name}</el-button>
      <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="tableData"
      @selection-change="handleSelectionChange"
      v-loading="loading"
    >
      <el-table-column type="selection" width="55" />
${config.columns.map(col => `      <el-table-column prop="${col.prop}" label="${col.label}"${col.width ? ` width="${col.width}"` : ''}${col.format ? `>\n        <template #default="{ row }">\n          ${col.format === 'price' ? '¥{{ row.' + col.prop + '?.toFixed(2) }}' : ''}\n          ${col.format === 'stock' ? '<el-tag :type="row.' + col.prop + ' <= row.warningStock ? \'danger\' : \'success\'">{{ row.' + col.prop + ' }}</el-tag>' : ''}\n        </template>\n      </el-table-column>` : ' />').join('\n')}
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchData"
      @current-change="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { get${config.nameEn}List, delete${config.nameEn}, batchDelete${config.nameEn}s } from '@/api/${config.apiPath}';

// 搜索表单
const searchForm = reactive({
${config.columns.slice(0, 3).map(col => `  ${col.prop}: ''`).join(',\n')}
});

// 表格数据
const tableData = ref([]);
const loading = ref(false);
const selectedIds = ref<number[]>([]);

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    const { data } = await get${config.nameEn}List({
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    });
    tableData.value = data.records;
    pagination.total = data.total;
  } catch (error) {
    ElMessage.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  fetchData();
};

// 重置
const handleReset = () => {
${config.columns.slice(0, 3).map(col => `  searchForm.${col.prop} = '';`).join('\n')}
  handleSearch();
};

// 新增
const handleAdd = () => {
  // 打开新增对话框
};

// 编辑
const handleEdit = (row: any) => {
  // 打开编辑对话框
};

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除吗？', '提示', {
      type: 'warning'
    });
    await delete${config.nameEn}(row.id);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id);
};

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(\`确定要删除选中的 \${selectedIds.value.length} 条记录吗？\`, '提示', {
      type: 'warning'
    });
    await batchDelete${config.nameEn}s(selectedIds.value);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.${config.apiPath}-list {
  padding: 20px;

  .search-form {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
  }

  .toolbar {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
`;
}

/**
 * 生成 Spring Boot 实体类模板
 * @param {string} tableName - 表名
 * @param {string} className - 类名
 * @param {Array} fields - 字段定义 [{name, type, comment}]
 * @returns {string} Java 实体类代码
 */
function generateEntity(tableName, className, fields) {
  const fieldMappings = {
    string: 'String',
    integer: 'Integer',
    long: 'Long',
    bigdecimal: 'BigDecimal',
    date: 'LocalDateTime',
    boolean: 'Boolean'
  };

  return `package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * ${fields.find(f => f.name === 'comment')?.comment || className}
 */
@Data
@TableName("${tableName}")
public class ${className} {

    @TableId(type = IdType.AUTO)
    private Long id;

${fields.filter(f => f.name !== 'id').map(f => {
  const javaType = fieldMappings[f.type.toLowerCase()] || 'String';
  const comment = f.comment || f.name;
  return `    /** ${comment} */
    private ${javaType} ${f.name};`;
}).join('\n\n')}

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}`;
}

/**
 * 生成 Spring Boot Service 层模板
 * @param {string} className - 类名
 * @returns {string} Java Service 代码
 */
function generateService(className) {
  const mapperName = className.replace(/Entity$/, '') + 'Mapper';
  const serviceName = className.replace(/Entity$/, '') + 'Service';

  return `package com.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.inventory.entity.${className};
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.dto.QueryDTO;

/**
 * ${className.replace(/Entity$/, '')} 服务接口
 */
public interface ${serviceName} extends IService<${className}> {

    /**
     * 分页查询
     */
    IPage<${className}> page(QueryDTO query);

    /**
     * 根据业务条件查询
     */
    ${className} getByCondition(String key);
}
`;
}

/**
 * 生成 Spring Boot Controller 层模板
 * @param {string} className - 类名
 * @param {string} pathPrefix - 路径前缀
 * @returns {string} Java Controller 代码
 */
function generateController(className, pathPrefix) {
  const entityName = className.replace(/Entity$/, '');
  const serviceName = entityName + 'Service';

  return `package com.inventory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.service.${serviceName};
import com.inventory.entity.${className};
import com.inventory.common.Result;
import com.inventory.dto.QueryDTO;

/**
 * ${entityName} 控制器
 */
@RestController
@RequestMapping("/api/${pathPrefix}")
public class ${entityName}Controller {

    @Autowired
    private ${serviceName} ${pathPrefix}Service;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<IPage<${className}>> page(QueryDTO query) {
        return Result.success(${pathPrefix}Service.page(query));
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public Result<${className}> getById(@PathVariable Long id) {
        return Result.success(${pathPrefix}Service.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> save(@RequestBody @Validated ${className} entity) {
        ${pathPrefix}Service.save(entity);
        return Result.success();
    }

    /**
     * 更新
     */
    @PutMapping
    public Result<Void> update(@RequestBody @Validated ${className} entity) {
        ${pathPrefix}Service.updateById(entity);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ${pathPrefix}Service.removeById(id);
        return Result.success();
    }
}
`;
}

/**
 * 生成 MyBatis-Plus Mapper XML 模板
 * @param {string} tableName - 表名
 * @param {string} className - 类名
 * @returns {string} Mapper XML 代码
 */
function generateMapperXML(tableName, className) {
  const entityName = className.replace(/Entity/, '').toLowerCase();

  return `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.inventory.mapper.${className.replace(/Entity/, '')}Mapper">

    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="com.inventory.entity.${className}">
        <id column="id" property="id" />
        <result column="create_time" property="createTime" />
        <result column="update_time" property="updateTime" />
    </resultMap>

    <!-- 通用查询条件 -->
    <sql id="queryCondition">
        <where>
            <if test="name != null and name != ''">
                AND name LIKE CONCAT('%', #{name}, '%')
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
        </where>
    </sql>

    <!-- 分页查询 -->
    <select id="selectPage" resultMap="BaseResultMap">
        SELECT * FROM ${tableName}
        <include refid="queryCondition" />
        ORDER BY id DESC
    </select>

</mapper>
`;
}

module.exports = {
  generateInventoryListPage,
  generateEntity,
  generateService,
  generateController,
  generateMapperXML
};
