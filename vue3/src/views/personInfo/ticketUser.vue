<template>
  <!--个人信息-->
  <div class="container">
    <Header></Header>
    <div class="red-line"></div>
    <div class="section">
      <MenuSideBar class="sidebarMenu" activeIndex="4"></MenuSideBar>
      <div class="right-section">
        <div class="breadcrumb"><span>常用购票人管理</span></div>
        <div class="right-tab">
          <div class="action-bar" v-if="isShow">
             <el-button type="primary" class="add-btn" @click="addTicketUser">
               <el-icon><Plus /></el-icon>新增购票人
             </el-button>
          </div>
          <div class="table-container" v-if="isShow">
            <el-table :data="ticketUserListData" style="width: 100%" border :header-cell-style="{background:'#f5f7fa',color:'#606266'}">
              <el-table-column  type="index"  label="序号" width="80"   align="center"/>
              <el-table-column prop="relName" label="姓名"    align="center"/>
              <el-table-column prop="index" label="证件类型"  align="center">
                <template #default="scope">
                  {{ getIdTypeName(scope.row.idType)}}
                </template>
              </el-table-column>
              <el-table-column prop="idNumber" label="证件号"   align="center" />
              <el-table-column prop="index" label="操作"   align="center" width="120">
                <template #default="scope">
                  <el-button link type="danger" icon="Delete" @click="delTicketUser(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="addTicketUserInfo"  v-if="!isShow">
            <div class="form-header">
              <span class="title">新增购票人信息</span>
            </div>
            <el-form ref="ticketRef" :model="formTicket" :rules="formTicketRules" class="ticketForm" label-width="100px" status-icon>
              <el-form-item label="姓名" prop="relName">
                <el-input
                    v-model="formTicket.relName"
                    type="text"
                    placeholder="请输入姓名"
                    class="custom-input"
                ></el-input>
              </el-form-item>
              
              <el-form-item label="证件类型" prop="idType">
                <el-select v-model="formTicket.idType" class="custom-select" placeholder="请选择证件类型">
                  <el-option v-for="item in idType"
                             :key="item.value"
                             :value="item.value"
                             :label="item.name">{{item.name}}</el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="证件号码" prop="idNumber">
                <el-input
                    v-model="formTicket.idNumber"
                    type="text"
                    placeholder="请输入证件号码"
                    class="custom-input"
                ></el-input>
              </el-form-item>
              
             <el-form-item class="form-footer">
               <el-button type="primary" class="save-btn" @click.prevent="saveTicket">保存</el-button>
               <el-button class="cancel-btn" @click.prevent="closeTicket">取消</el-button>
             </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
    <Footer class="foot"></Footer>
  </div>
</template>

<script setup name="TicketUser">
import MenuSideBar from '../../components/menuSidebar/index'
import Header from '../../components/header/index'
import Footer from '../../components/footer/index'
import {ref, computed, onMounted, reactive, getCurrentInstance} from 'vue'
import useUserStore from "../../store/modules/user";
import {delTicketUserApi, selectTicketUserListApi} from '@/api/accountCenter.js'
import {getIdTypeName} from '@/api/common.js'
import {ElMessage, ElMessageBox} from 'element-plus'
import { getUserIdKey} from "@/utils/auth";
import {saveTicketUser} from "@/api/buyTicketUser";
import { Plus, Delete } from '@element-plus/icons-vue'


const {proxy} = getCurrentInstance();
const useUser = useUserStore()
//购票人列表入参
const ticketUserListParams = reactive({
  userId:undefined
})
const formTicket = ref({})
formTicket.value.idType = ref('1')

const formTicketRules = ref({
  relName:  [{ required: true, message: "请输入姓名", trigger: "blur" }],
  idNumber:[{ required: true, message: "请输入证件号码", trigger: "blur" }],
})
//购票人列表数据
const ticketUserListData = ref([])
const isShow = ref(true)

const idType = ref([{
  name:'身份证',
  value:'1'
},{
  name:'港澳台居民居住证',
  value:'2'
},{
  name:'港澳台居民来往内地通行证',
  value:'3'
},{
  name:'台湾居民来往内地通行证',
  value:'4'
},{
  name:'护照',
  value:'5'
},{
  name:'歪果仁永久居住证',
  value:'6'
}])
// 获取路由参数
ticketUserListParams.userId = useUser.userId
selectTicketUserList()
//购票人列表方法
function selectTicketUserList() {
  selectTicketUserListApi(ticketUserListParams).then(response => {
    ticketUserListData.value = response.data;
  })
}

function delTicketUser(ticketUserId){
  ElMessageBox.confirm('确定删除该购票人吗？删除后不可恢复', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const delTicketUserParam = { id: ticketUserId }
    delTicketUserApi(delTicketUserParam).then(() => {
      ElMessage({ message: '删除成功', type: 'success' })
      selectTicketUserList()
    }).catch(() => {
      ElMessage({ message: '删除失败', type: 'error' })
    })
  }).catch(() => {})
}
//新增购票人
function addTicketUser(){
  isShow.value =false
  reset()
}



//保存
function saveTicket(){
  proxy.$refs.ticketRef.validate(valid => {
    if (valid) {
      formTicket.value.userId=getUserIdKey()
      saveTicketUser(formTicket.value).then(response=>{
        if(response.code==0){
          isShow.value = true
          selectTicketUserList()
          reset()
        }
      })


    }
  });
}
//取消
function closeTicket(){
  isShow.value =true
  reset()
}

function reset(){
  formTicket.value= {}
  formTicket.value.idType = ref('1')
}
</script>

<style scoped lang="scss">
.container{
  .red-line {
    border-bottom: 5px solid rgba(255, 55, 29, 0.85);
  }

  .section {
    width: 1000px;
    margin: 15px auto 0;

    .sidebarMenu {
      //width: 201px;
      float: left;
    }

    .right-section {
      width: 789px;
      float: right;

      .breadcrumb {
        border: 1px solid #efefef;
        height: 38px;
        overflow: hidden;
        background: #FF2D55 repeat-x;
        padding: 0 15px;
        line-height: 38px;
        color: #ffffff;
        margin-bottom: 15px;
      }

      .right-tab {
        margin-top: 23px;
        min-height: 500px;
        
        .action-bar {
          display: flex;
          justify-content: flex-end;
          margin-bottom: 20px;
          
          .add-btn {
            background-color: #FF2D55;
            border-color: #FF2D55;
            color: #fff;
            padding: 10px 20px;
            
            &:hover {
              background-color: #ff5c7c;
              border-color: #ff5c7c;
            }
          }
        }

        .table-container {
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
          border-radius: 4px;
          overflow: hidden;
        }

        .addTicketUserInfo{
          background: #fff;
          border-radius: 8px;
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
          padding: 30px 40px;
          
          .form-header {
            border-bottom: 1px solid #ebeef5;
            padding-bottom: 20px;
            margin-bottom: 30px;
            
            .title{
              font-size: 18px;
              color: #333;
              font-weight: 600;
              position: relative;
              padding-left: 12px;
              
              &:before {
                content: '';
                position: absolute;
                left: 0;
                top: 4px;
                height: 16px;
                width: 4px;
                background: #FF2D55;
                border-radius: 2px;
              }
            }
          }

          .ticketForm{
            max-width: 600px;
            
            :deep(.el-form-item) {
              margin-bottom: 24px;
            }
            
            .custom-input,
            .custom-select {
              width: 100%;
            }

            .form-footer {
              margin-top: 40px;
              
              .save-btn {
                background-color: #FF2D55;
                border-color: #FF2D55;
                width: 120px;
                
                &:hover {
                  background-color: #ff5c7c;
                  border-color: #ff5c7c;
                }
              }
              
              .cancel-btn {
                margin-left: 20px;
                width: 100px;
                
                &:hover {
                  color: #FF2D55;
                  border-color: #FF2D55;
                  background-color: #fff5f7;
                }
              }
            }
          }
        }
     }


    }

  }

}
</style>
