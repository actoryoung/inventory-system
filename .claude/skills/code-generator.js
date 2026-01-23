/**
 * Code Generator Skill
 *
 * 根据规范生成代码骨架和模板。
 */

/**
 * 生成 Vue 3 组件模板
 * @param {string} componentName - 组件名称
 * @param {boolean} withTypescript - 是否使用 TypeScript
 * @param {boolean} withComposition - 是否使用组合式 API
 * @returns {string} 组件代码
 */
function generateVueComponent(componentName, withTypescript = true, withComposition = true) {
  const ts = withTypescript;
  const composition = withComposition;

  if (composition) {
    return ts ? `
<template>
  <div class="${componentName.toLowerCase()}">
    <h2>{{ title || '${componentName}' }}</h2>
    <!-- 组件内容 -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

interface ${componentName}Props {
  title?: string;
  onAction?: () => void;
}

const props = withDefaults(defineProps<${componentName}Props>(), {
  title: '${componentName}'
});

const emit = defineEmits<{
  action: [];
}>();

const state = ref<string>('');

onMounted(() => {
  // 初始化逻辑
});

const handleClick = () => {
  emit('action');
};
</script>

<style scoped>
.${componentName.toLowerCase()} {
  /* 样式 */
}
</style>
` : `
<template>
  <div class="${componentName.toLowerCase()}">
    <h2>{{ title || '${componentName}' }}</h2>
    <!-- 组件内容 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const props = defineProps({
  title: {
    type: String,
    default: '${componentName}'
  }
});

const emit = defineEmits(['action']);

const state = ref('');

onMounted(() => {
  // 初始化逻辑
});

const handleClick = () => {
  emit('action');
};
</script>

<style scoped>
.${componentName.toLowerCase()} {
  /* 样式 */
}
</style>
`;
  }

  // Options API template
  return ts ? `
<template>
  <div class="${componentName.toLowerCase()}">
    <h2>{{ title || '${componentName}' }}</h2>
    <!-- 组件内容 -->
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: '${componentName}',
  props: {
    title: {
      type: String,
      default: '${componentName}'
    }
  },
  emits: ['action'],
  data() {
    return {
      state: ''
    };
  },
  mounted() {
    // 初始化逻辑
  },
  methods: {
    handleClick() {
      this.$emit('action');
    }
  }
});
</script>

<style scoped>
.${componentName.toLowerCase()} {
  /* 样式 */
}
</style>
` : `
<template>
  <div class="${componentName.toLowerCase()}">
    <h2>{{ title || '${componentName}' }}</h2>
    <!-- 组件内容 -->
  </div>
</template>

<script>
export default {
  name: '${componentName}',
  props: {
    title: {
      type: String,
      default: '${componentName}'
    }
  },
  emits: ['action'],
  data() {
    return {
      state: ''
    };
  },
  mounted() {
    // 初始化逻辑
  },
  methods: {
    handleClick() {
      this.$emit('action');
    }
  }
};
</script>

<style scoped>
.${componentName.toLowerCase()} {
  /* 样式 */
}
</style>
`;
}

/**
 * 生成 Spring Boot Controller 模板
 * @param {string} entityName - 实体名称
 * @param {string} packageName - 包名
 * @returns {string} Controller 代码
 */
function generateSpringController(entityName, packageName = 'com.example') {
  const entityLower = entityName.toLowerCase();
  const entityUpper = entityName.charAt(0).toUpperCase() + entityName.slice(1);

  return `
package ${packageName}.controller;

import ${packageName}.entity.${entityUpper};
import ${packageName}.service.${entityUpper}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ${entityUpper} 控制器
 */
@RestController
@RequestMapping("/api/${entityLower}")
@CrossOrigin(origins = "*")
public class ${entityUpper}Controller {

    @Autowired
    private ${entityUpper}Service ${entityLower}Service;

    /**
     * 获取所有${entityName}
     */
    @GetMapping
    public Result<List<${entityUpper}>> getAll() {
        List<${entityUpper}> list = ${entityLower}Service.findAll();
        return Result.success(list);
    }

    /**
     * 根据 ID 获取${entityName}
     */
    @GetMapping("/{id}")
    public Result<${entityUpper}> getById(@PathVariable Long id) {
        ${entityUpper} entity = ${entityLower}Service.findById(id);
        if (entity == null) {
            return Result.error("${entityName}不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建${entityName}
     */
    @PostMapping
    public Result<${entityUpper}> create(@RequestBody ${entityUpper} entity) {
        ${entityUpper} created = ${entityLower}Service.save(entity);
        return Result.success(created);
    }

    /**
     * 更新${entityName}
     */
    @PutMapping("/{id}")
    public Result<${entityUpper}> update(@PathVariable Long id, @RequestBody ${entityUpper} entity) {
        entity.setId(id);
        ${entityUpper} updated = ${entityLower}Service.save(entity);
        return Result.success(updated);
    }

    /**
     * 删除${entityName}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ${entityLower}Service.deleteById(id);
        return Result.success();
    }
}
`;
}

/**
 * 生成 MyBatis-Plus Service 模板
 * @param {string} entityName - 实体名称
 * @param {string} packageName - 包名
 * @returns {string} Service 代码
 */
function generateMyBatisService(entityName, packageName = 'com.example') {
  const entityLower = entityName.toLowerCase();
  const entityUpper = entityName.charAt(0).toUpperCase() + entityName.slice(1);

  return `
package ${packageName}.service;

import ${packageName}.entity.${entityUpper};
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * ${entityUpper} 服务接口
 */
public interface ${entityUpper}Service extends IService<${entityUpper}> {

    /**
     * 查询所有${entityName}
     */
    List<${entityUpper}> findAll();

    /**
     * 根据 ID 查询${entityName}
     */
    ${entityUpper} findById(Long id);

    /**
     * 保存${entityName}
     */
    ${entityUpper} save(${entityUpper} entity);

    /**
     * 删除${entityName}
     */
    void deleteById(Long id);
}
`;
}

module.exports = {
  generateVueComponent,
  generateSpringController,
  generateMyBatisService
};
