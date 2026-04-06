<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>

      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>

    <a-divider />
    <!--    表格-->

    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="72" :height="72" />
        </template>

        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>

          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>

        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>

        <template v-else-if="column.key === 'action'">
          <a-button danger>删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {deleteUser, listUserVoByPage} from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
//数据
const data = ref<API.UserVO[]>([])
const total = ref(0)
//分页参数
//搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const pagination = computed(()=>{
  return {
    current: searchParams.pageNum ?? 1,
    pageSize:searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number)=>`共 ${total} 条`,
  }
})

//获取数据
//async 函数，调用后端分页接口 listUserVoByPage
//使用展开运算符 ...searchParams 把 pageNum、pageSize 等参数传过去
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}
//表格变化处理
const doTableChange = (page:any)=>{
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

//获取数据
const doSearch = () =>{
  //重置页码
  searchParams.pageNum = 1
  fetchData()
}

//删除数据
const doDelete = async (id: string) => {
  if(!id){
    return
  }

  const res = await deleteUser({ id })
  if(res.data.code === 0){
    message.success('删除成功')
    //刷新数据
    fetchData()
  }else{
    message.error('删除失败')
  }
}


//当组件挂载（DOM 渲染完成）后，立即调用一次 fetchData
//实现“页面打开就显示用户列表”的效果
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  margin: 24px auto;
  padding: 28px 36px;
}

#userManagePage :deep(.ant-form) {
  background: #ffffff;
  padding: 20px 24px;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04), 0 0 0 1px rgba(0, 0, 0, 0.02);
  border: 1px solid #eef2f7;
  margin-bottom: 20px;
}

#userManagePage :deep(.ant-form-item) {
  margin-right: 16px;
  margin-bottom: 0;
}

#userManagePage :deep(.ant-form-item-label) {
  font-weight: 500;
  color: #475569;
}

#userManagePage :deep(.ant-input) {
  border-radius: 8px;
}

#userManagePage :deep(.ant-btn-primary) {
  border-radius: 8px;
  font-weight: 500;
  height: 36px;
  padding: 0 18px;
}

#userManagePage :deep(.ant-divider) {
  display: none;
}

#userManagePage :deep(.ant-table-wrapper) {
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.05), 0 0 0 1px rgba(0, 0, 0, 0.03);
  border: 1px solid #eef2f7;
  overflow-x: auto;
  padding: 4px 0;
}

#userManagePage :deep(.ant-table) {
  border-radius: 14px;
}

#userManagePage :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, #f8faff 0%, #f0f5ff 100%);
  font-weight: 600;
  color: #374151;
  font-size: 13px;
  letter-spacing: 0.3px;
  padding: 12px 14px;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

#userManagePage :deep(.ant-table-tbody > tr > td) {
  padding: 12px 14px;
  color: #475569;
  font-size: 13px;
  transition: background 0.15s ease;
}

#userManagePage :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(99, 102, 241, 0.03);
}

#userManagePage :deep(.ant-table-tbody > .ant-table-row:last-child > td) {
  border-bottom: none;
}

#userManagePage :deep(.ant-tag) {
  border-radius: 6px;
  font-weight: 500;
  padding: 1px 10px;
  font-size: 12px;
}

#userManagePage :deep(.ant-image) {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.08);
}

#userManagePage :deep(.ant-image-img) {
  object-fit: cover;
}

#userManagePage :deep(.ant-pagination) {
  margin: 20px 0 0;
  padding: 0 16px 16px;
}
</style>
