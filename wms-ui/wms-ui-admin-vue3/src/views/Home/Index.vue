<template>
  <div class="wms-home">
    <ContentWrap :body-style="{ padding: '24px' }">
      <div class="flex flex-wrap items-center justify-between gap-16px">
        <div>
          <h2 class="flex items-center m-0 mb-8px text-26px font-600 text-[#303133]">
            <Icon icon="ep:box" class="mr-12px text-[#409eff]" />
            捷圣 WMS 仓储工作台
          </h2>
          <p class="m-0 text-[#606266] text-14px">
            聚焦入库、出库、库存、库位与异常作业，帮助仓库现场快速掌握当日运行状态。
          </p>
        </div>
        <div class="flex flex-wrap gap-10px">
          <el-tag effect="dark" type="success">系统运行正常</el-tag>
          <el-tag type="info">今日班次：早班</el-tag>
        </div>
      </div>
    </ContentWrap>

    <el-row :gutter="16" class="mb-16px">
      <el-col v-for="item in overviewCards" :key="item.label" :lg="6" :md="12" :sm="12" :xs="24">
        <el-card class="mb-16px transition-all duration-300 hover:-translate-y-2px" shadow="hover">
          <div class="flex items-center">
            <div
              class="w-52px h-52px rounded-10px flex items-center justify-center text-26px text-white mr-16px"
              :class="item.bg"
            >
              <Icon :icon="item.icon" />
            </div>
            <div class="flex-1">
              <div class="text-26px font-600 text-[#303133] leading-none">{{ item.value }}</div>
              <div class="text-14px text-[#909399] mt-6px">{{ item.label }}</div>
              <div class="text-12px text-[#67c23a] mt-8px">{{ item.trend }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :lg="16" :md="24" :xs="24">
        <ContentWrap title="作业看板" :body-style="{ padding: '18px' }">
          <el-row :gutter="14">
            <el-col v-for="task in operationTasks" :key="task.name" :md="6" :sm="12" :xs="24">
              <div class="task-card mb-14px">
                <div class="flex items-center justify-between mb-12px">
                  <span class="text-15px font-500 text-[#303133]">{{ task.name }}</span>
                  <el-tag :type="task.type" size="small">{{ task.status }}</el-tag>
                </div>
                <div class="text-28px font-600 text-[#303133] mb-10px">{{ task.count }}</div>
                <el-progress :percentage="task.percent" :stroke-width="8" />
              </div>
            </el-col>
          </el-row>
        </ContentWrap>

        <ContentWrap title="库区容量" :body-style="{ padding: '18px' }">
          <div v-for="area in warehouseAreas" :key="area.name" class="mb-18px last:mb-0">
            <div class="flex items-center justify-between mb-8px">
              <div class="flex items-center">
                <Icon icon="ep:office-building" class="mr-8px text-[#409eff]" />
                <span class="text-14px font-500 text-[#303133]">{{ area.name }}</span>
              </div>
              <span class="text-13px text-[#909399]">{{ area.used }} / {{ area.total }} 库位</span>
            </div>
            <el-progress :percentage="area.percent" :status="area.status" :stroke-width="10" />
          </div>
        </ContentWrap>
      </el-col>

      <el-col :lg="8" :md="24" :xs="24">
        <ContentWrap title="库存预警" :body-style="{ padding: '18px' }">
          <div v-for="warning in inventoryWarnings" :key="warning.title" class="warning-item">
            <div class="flex items-start">
              <Icon :icon="warning.icon" class="mt-2px mr-10px" :class="warning.color" />
              <div class="flex-1">
                <div class="flex items-center justify-between mb-4px">
                  <span class="text-14px font-500 text-[#303133]">{{ warning.title }}</span>
                  <el-tag :type="warning.type" size="small">{{ warning.count }} 条</el-tag>
                </div>
                <div class="text-12px text-[#909399]">{{ warning.desc }}</div>
              </div>
            </div>
          </div>
        </ContentWrap>

        <ContentWrap title="快捷入口" :body-style="{ padding: '18px' }">
          <el-row :gutter="12">
            <el-col v-for="entry in quickEntries" :key="entry.name" :span="12">
              <div class="quick-entry mb-12px">
                <Icon :icon="entry.icon" class="text-24px mb-8px" :class="entry.color" />
                <div class="text-14px text-[#303133]">{{ entry.name }}</div>
              </div>
            </el-col>
          </el-row>
        </ContentWrap>

        <ContentWrap title="设备状态" :body-style="{ padding: '18px' }">
          <div
            v-for="device in deviceStatus"
            :key="device.name"
            class="flex items-center justify-between mb-14px last:mb-0"
          >
            <div class="flex items-center">
              <span class="status-dot mr-10px" :class="device.dot"></span>
              <span class="text-14px text-[#303133]">{{ device.name }}</span>
            </div>
            <el-tag :type="device.type" size="small">{{ device.status }}</el-tag>
          </div>
        </ContentWrap>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'Index' })

const overviewCards = [
  {
    label: '库存 SKU',
    value: '12,486',
    trend: '较昨日 +128',
    icon: 'ep:goods',
    bg: 'bg-gradient-to-br from-[#667eea] to-[#764ba2]'
  },
  {
    label: '可用库位',
    value: '3,216',
    trend: '可用率 72%',
    icon: 'ep:grid',
    bg: 'bg-gradient-to-br from-[#36d1dc] to-[#5b86e5]'
  },
  {
    label: '今日入库',
    value: '428',
    trend: '已完成 86%',
    icon: 'ep:bottom-left',
    bg: 'bg-gradient-to-br from-[#11998e] to-[#38ef7d]'
  },
  {
    label: '今日出库',
    value: '516',
    trend: '准时率 98%',
    icon: 'ep:top-right',
    bg: 'bg-gradient-to-br from-[#ff9966] to-[#ff5e62]'
  }
]

const operationTasks = [
  { name: '待上架', count: 36, percent: 68, status: '进行中', type: 'primary' as const },
  { name: '待拣货', count: 52, percent: 74, status: '高峰', type: 'warning' as const },
  { name: '待复核', count: 18, percent: 42, status: '正常', type: 'success' as const },
  { name: '异常任务', count: 7, percent: 18, status: '需处理', type: 'danger' as const }
]

const warehouseAreas = [
  { name: 'A 区 - 原料库', used: 856, total: 1200, percent: 71, status: undefined },
  { name: 'B 区 - 成品库', used: 1048, total: 1300, percent: 81, status: 'warning' as const },
  { name: 'C 区 - 备件库', used: 392, total: 900, percent: 44, status: 'success' as const },
  { name: 'D 区 - 暂存区', used: 188, total: 260, percent: 72, status: undefined }
]

const inventoryWarnings = [
  {
    title: '低库存预警',
    count: 12,
    desc: '安全库存不足，需要及时补货',
    icon: 'ep:warning-filled',
    color: 'text-[#e6a23c]',
    type: 'warning' as const
  },
  {
    title: '超储提醒',
    count: 5,
    desc: '部分库位容量接近上限',
    icon: 'ep:circle-close-filled',
    color: 'text-[#f56c6c]',
    type: 'danger' as const
  },
  {
    title: '临期物料',
    count: 9,
    desc: '建议优先安排先进先出',
    icon: 'ep:timer',
    color: 'text-[#409eff]',
    type: 'primary' as const
  },
  {
    title: '冻结库存',
    count: 3,
    desc: '质检或盘点锁定库存',
    icon: 'ep:lock',
    color: 'text-[#909399]',
    type: 'info' as const
  }
]

const quickEntries = [
  { name: '入库管理', icon: 'ep:download', color: 'text-[#409eff]' },
  { name: '出库管理', icon: 'ep:upload', color: 'text-[#67c23a]' },
  { name: '库存查询', icon: 'ep:search', color: 'text-[#e6a23c]' },
  { name: '库位管理', icon: 'ep:map-location', color: 'text-[#f56c6c]' }
]

const deviceStatus = [
  { name: '堆垛机', status: '在线', type: 'success' as const, dot: 'bg-[#67c23a]' },
  { name: '输送线', status: '在线', type: 'success' as const, dot: 'bg-[#67c23a]' },
  { name: 'AGV 小车', status: '忙碌', type: 'warning' as const, dot: 'bg-[#e6a23c]' },
  { name: '扫码设备', status: '在线', type: 'success' as const, dot: 'bg-[#67c23a]' }
]
</script>

<style lang="scss" scoped>
.wms-home {
  .task-card,
  .quick-entry,
  .warning-item {
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 10px;
    background: var(--el-bg-color);
  }

  .task-card {
    padding: 16px;
  }

  .warning-item {
    padding: 14px 0;
    border-width: 0 0 1px;
    border-radius: 0;

    &:first-child {
      padding-top: 0;
    }

    &:last-child {
      padding-bottom: 0;
      border-bottom: 0;
    }
  }

  .quick-entry {
    padding: 16px 8px;
    text-align: center;
    cursor: default;
    transition: all 0.2s ease;

    &:hover {
      border-color: var(--el-color-primary-light-5);
      background: var(--el-color-primary-light-9);
    }
  }

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}
</style>
