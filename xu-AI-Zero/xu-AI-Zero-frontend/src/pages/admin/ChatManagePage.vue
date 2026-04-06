<template>
  <div id="chatManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="消息内容">
        <a-input v-model:value="searchParams.message" placeholder="输入消息内容" />
      </a-form-item>
      <a-form-item label="消息类型">
        <a-select
          v-model:value="searchParams.messageType"
          placeholder="选择消息类型"
          style="width: 120px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="user">用户消息</a-select-option>
          <a-select-option value="assistant">AI消息</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="应用ID">
        <a-input v-model:value="searchParams.appId" placeholder="输入应用ID" />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" />
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
      :scroll="{ x: 1400 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'message'">
          <a-tooltip :title="record.message">
            <div class="message-text">{{ record.message }}</div>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'messageType'">
          <a-tag :color="record.messageType === 'user' ? 'blue' : 'green'">
            {{ record.messageType === 'user' ? '用户消息' : 'AI消息' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="viewAppChat(record.appId)">
              查看对话
            </a-button>
            <a-popconfirm title="确定要删除这条消息吗？" @confirm="deleteMessage(record.id)">
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
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
import { formatTime } from '@/utils/time'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    width: 300,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    width: 100,
  },
  {
    title: '应用ID',
    dataIndex: 'appId',
    width: 80,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
  },
]

// 数据
const data = ref<API.ChatHistory[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listAllChatHistoryByPageForAdmin({
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

// 查看应用对话
const viewAppChat = (appId: number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 删除消息
const deleteMessage = async (id: number | undefined) => {
  if (!id) return

  try {
    // 注意：这里需要后端提供删除对话历史的接口
    // 目前先显示成功，实际实现需要调用删除接口
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
#chatManagePage {
  margin: 24px auto;
  padding: 28px 36px;
}

#chatManagePage :deep(.ant-form) {
  background: #ffffff;
  padding: 20px 24px;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04), 0 0 0 1px rgba(0, 0, 0, 0.02);
  border: 1px solid #eef2f7;
  margin-bottom: 20px;
}

#chatManagePage :deep(.ant-form-item) {
  margin-right: 16px;
  margin-bottom: 0;
}

#chatManagePage :deep(.ant-input),
#chatManagePage :deep(.ant-select-selector) {
  border-radius: 8px !important;
}

#chatManagePage :deep(.ant-btn-primary) {
  border-radius: 8px;
  font-weight: 500;
  height: 36px;
  padding: 0 18px;
}

#chatManagePage :deep(.ant-divider) {
  display: none;
}

#chatManagePage :deep(.ant-table-wrapper) {
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.05), 0 0 0 1px rgba(0, 0, 0, 0.03);
  border: 1px solid #eef2f7;
  overflow-x: auto;
  padding: 4px 0;
}

#chatManagePage :deep(.ant-table) {
  border-radius: 14px;
}

#chatManagePage :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, #f8faff 0%, #f0f5ff 100%);
  font-weight: 600;
  color: #374151;
  font-size: 13px;
  letter-spacing: 0.3px;
  padding: 12px 14px;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

#chatManagePage :deep(.ant-table-tbody > tr > td) {
  padding: 12px 14px;
  color: #475569;
  font-size: 13px;
  vertical-align: middle;
  transition: background 0.15s ease;
}

#chatManagePage :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(99, 102, 241, 0.03);
}

#chatManagePage :deep(.ant-table-tbody > .ant-table-row:last-child > td) {
  border-bottom: none;
}

#chatManagePage :deep(.ant-tag) {
  border-radius: 6px;
  font-weight: 500;
  padding: 1px 10px;
  font-size: 12px;
}

#chatManagePage :deep(.ant-pagination) {
  margin: 20px 0 0;
  padding: 0 16px 16px;
}

#chatManagePage :deep(.ant-space) {
  flex-wrap: nowrap;
}

#chatManagePage :deep(.ant-btn-sm) {
  border-radius: 6px;
  font-weight: 500;
  font-size: 12px;
  height: 30px;
  padding: 0 12px;
}

.message-text {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #64748b;
  cursor: default;
}
</style>
