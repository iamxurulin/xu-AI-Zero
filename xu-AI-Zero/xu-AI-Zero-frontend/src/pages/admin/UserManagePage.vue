<template>
  <div id="userManagePage">
    <!--    搜索表单-->
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
          <a-image :src="record.userAvatar" :width="120" />
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

<style scoped></style>
