<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" />
      </a-form-item>
      <a-form-item label="创建者">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" />
      </a-form-item>
      <a-form-item label="生成类型">
        <a-select
          v-model:value="searchParams.codeGenType"
          placeholder="选择生成类型"
          style="width: 150px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option
            v-for="option in CODE_GEN_TYPE_OPTIONS"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
          <a-image v-if="record.cover" :src="record.cover" :width="80" :height="60" />
          <div v-else class="no-cover">无封面</div>
        </template>
        <template v-else-if="column.dataIndex === 'initPrompt'">
          <a-tooltip :title="record.initPrompt">
            <div class="prompt-text">{{ record.initPrompt }}</div>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'codeGenType'">
          {{ formatCodeGenType(record.codeGenType) }}
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag v-if="record.priority === 99" color="gold">精选</a-tag>
          <span v-else>{{ record.priority || 0 }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'deployedTime'">
          <span v-if="record.deployedTime">
            {{ formatTime(record.deployedTime) }}
          </span>
          <span v-else class="text-gray">未部署</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.dataIndex === 'user'">
          <UserInfo :user="record.user" size="small" />
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="editApp(record)"> 编辑 </a-button>
            <a-button
              type="default"
              size="small"
              @click="toggleFeatured(record)"
              :class="{ 'featured-btn': record.priority === 99 }"
            >
              {{ record.priority === 99 ? '取消精选' : '精选' }}
            </a-button>
            <a-popconfirm title="确定要删除这个应用吗？" @confirm="deleteApp(record.id)">
              <a-button danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listAppVoByPageByAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    width: 150,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    width: 200,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 80,
  },
  {
    title: '部署时间',
    dataIndex: 'deployedTime',
    width: 160,
  },
  {
    title: '创建者',
    dataIndex: 'user',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 240,
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listAppVoByPageByAdmin({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 切换精选状态
const toggleFeatured = async (app: API.AppVO) => {
  if (!app.id) return

  const newPriority = app.priority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({
      id: app.id,
      priority: newPriority,
    })

    if (res.data.code === 0) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      // 刷新数据
      fetchData()
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  }
}

// 删除应用
const deleteApp = async (id: number | undefined) => {
  if (!id) return

  try {
    const res = await deleteAppByAdmin({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
#appManagePage {
  margin: 24px auto;
  padding: 28px 36px;
}

#appManagePage :deep(.ant-form) {
  background: #ffffff;
  padding: 20px 24px;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04), 0 0 0 1px rgba(0, 0, 0, 0.02);
  border: 1px solid #eef2f7;
  margin-bottom: 20px;
}

#appManagePage :deep(.ant-form-item) {
  margin-right: 16px;
  margin-bottom: 0;
}

#appManagePage :deep(.ant-input),
#appManagePage :deep(.ant-select-selector) {
  border-radius: 8px !important;
}

#appManagePage :deep(.ant-btn-primary) {
  border-radius: 8px;
  font-weight: 500;
  height: 36px;
  padding: 0 18px;
}

#appManagePage :deep(.ant-divider) {
  display: none;
}

#appManagePage :deep(.ant-table-wrapper) {
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.05), 0 0 0 1px rgba(0, 0, 0, 0.03);
  border: 1px solid #eef2f7;
  overflow-x: auto;
  padding: 4px 0;
}

#appManagePage :deep(.ant-table) {
  border-radius: 14px;
}

#appManagePage :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, #f8faff 0%, #f0f5ff 100%);
  font-weight: 600;
  color: #374151;
  font-size: 13px;
  letter-spacing: 0.3px;
  padding: 12px 14px;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

#appManagePage :deep(.ant-table-tbody > tr > td) {
  padding: 12px 14px;
  color: #475569;
  font-size: 13px;
  vertical-align: middle;
  transition: background 0.15s ease;
}

#appManagePage :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(99, 102, 241, 0.03);
}

#appManagePage :deep(.ant-table-tbody > .ant-table-row:last-child > td) {
  border-bottom: none;
}

#appManagePage :deep(.ant-tag) {
  border-radius: 6px;
  font-weight: 500;
  padding: 1px 10px;
  font-size: 12px;
}

#appManagePage :deep(.ant-image) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.08);
}

#appManagePage :deep(.ant-image-img) {
  object-fit: cover;
}

#appManagePage :deep(.ant-pagination) {
  margin: 20px 0 0;
  padding: 0 16px 16px;
}

#appManagePage :deep(.ant-space) {
  flex-wrap: nowrap;
}

#appManagePage :deep(.ant-btn-sm) {
  border-radius: 6px;
  font-weight: 500;
  font-size: 12px;
  height: 30px;
  padding: 0 12px;
}

.no-cover {
  width: 72px;
  height: 52px;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 11px;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
}

.prompt-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #64748b;
  cursor: default;
}

.text-gray {
  color: #94a3b8;
  font-size: 12px;
}

.featured-btn {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border-color: transparent;
  color: white;
  border-radius: 6px;
  font-weight: 600;
}

.featured-btn:hover {
  background: linear-gradient(135deg, #d97706, #b45309);
}
</style>
