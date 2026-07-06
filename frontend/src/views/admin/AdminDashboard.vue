<template>
  <el-container class="merchant-admin">
    <el-aside :width="isCollapsed ? '76px' : '276px'" :class="['merchant-admin__aside', { collapsed: isCollapsed }]">
      <div class="merchant-admin__brand">
        <div class="merchant-admin__logo">温</div>
        <div v-show="!isCollapsed">
          <strong>温暖到家</strong>
          <span>Merchant OS</span>
        </div>
      </div>

      <button class="merchant-admin__collapse" type="button" @click="isCollapsed = !isCollapsed">
        <el-icon><component :is="isCollapsed ? Expand : Fold" /></el-icon>
        <span v-show="!isCollapsed">收起导航</span>
      </button>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        class="merchant-admin__menu"
        @select="activeMenu = $event"
      >
        <el-menu-item v-for="item in menuItems" :key="item.key" :index="item.key">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>
            <span>{{ item.label }}</span>
            <em v-if="item.badge">{{ item.badge }}</em>
          </template>
        </el-menu-item>
      </el-menu>

      <div v-show="!isCollapsed" class="merchant-admin__aside-card">
        <span>结算账户</span>
        <strong>mch_settlement_6222</strong>
        <small>订单支付后进入商户模拟结算账户</small>
      </div>

      <div v-show="!isCollapsed" class="merchant-admin__operator">
        <el-avatar :icon="UserFilled" />
        <div>
          <strong>{{ authUser?.name || '商户管理员' }}</strong>
          <span>{{ authUser?.username || 'admin' }} · 在线</span>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="merchant-admin__header">
        <div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>商户后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ activeTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
          <h1>{{ activeTitle }}</h1>
          <p>服务商品、技师履约、订单售后与经营数据统一管理</p>
        </div>
        <div class="merchant-admin__header-actions">
          <el-input
            v-model="keyword"
            :prefix-icon="Search"
            clearable
            placeholder="搜索订单、客户、服务"
          />
          <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
          <el-button @click="logout">退出</el-button>
          <el-avatar>商</el-avatar>
        </div>
      </el-header>

      <el-main class="merchant-admin__main">
        <section v-if="activeMenu === 'overview'" class="admin-metric-grid">
          <article v-for="metric in metrics" :key="metric.label" class="admin-metric-card">
            <div :class="['admin-metric-card__icon', metric.tone]">
              <el-icon><component :is="metric.icon" /></el-icon>
            </div>
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
            <small>{{ metric.hint }}</small>
          </article>
        </section>

        <section v-if="activeMenu === 'overview'" class="admin-content-grid">
          <el-card shadow="never" class="admin-panel admin-panel--wide">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>经营看板</strong>
                  <span>今日订单状态与履约分布</span>
                </div>
                <el-tag type="success">实时</el-tag>
              </div>
            </template>
            <div class="admin-status-board">
              <article v-for="item in statusSummary" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.count }}</strong>
                <el-progress :percentage="statusPercent(item.count)" :show-text="false" />
              </article>
              <el-empty v-if="!statusSummary.length" description="暂无订单数据" />
            </div>
          </el-card>

          <el-card shadow="never" class="admin-panel">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>待办事项</strong>
                  <span>需要商户关注的工作</span>
                </div>
              </div>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="task in tasks"
                :key="task.title"
                :type="task.type"
                :timestamp="task.timestamp"
              >
                <strong>{{ task.title }}</strong>
                <p>{{ task.description }}</p>
              </el-timeline-item>
            </el-timeline>
          </el-card>

          <el-card shadow="never" class="admin-panel admin-panel--wide">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>最新订单</strong>
                  <span>最近创建的服务订单</span>
                </div>
                <el-button link type="primary" @click="activeMenu = 'orders'">查看全部</el-button>
              </div>
            </template>
            <el-table :data="filteredOrders.slice(0, 6)" stripe>
              <el-table-column label="订单" width="132">
                <template #default="{ row }">{{ shortId(row.id) }}</template>
              </el-table-column>
              <el-table-column label="服务">
                <template #default="{ row }">{{ serviceName(row.serviceId) }}</template>
              </el-table-column>
              <el-table-column label="客户">
                <template #default="{ row }">{{ row.address?.contactName || '-' }}</template>
              </el-table-column>
              <el-table-column label="金额" width="120">
                <template #default="{ row }">¥ {{ money(row.payableAmount) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="150">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.status)">{{ statusText[row.status] ?? row.status }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </section>

        <section v-if="activeMenu === 'orders'" class="admin-stack">
          <el-card shadow="never" class="admin-panel">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>订单管理</strong>
                  <span>按状态筛选、查看履约轨迹和地址信息</span>
                </div>
                <el-segmented v-model="orderTab" :options="orderTabOptions" />
              </div>
            </template>
            <el-table :data="paginatedOrders" stripe height="560" @row-click="openOrder">
              <el-table-column label="订单号" width="150">
                <template #default="{ row }">
                  <el-link type="primary" :underline="false">{{ shortId(row.id) }}</el-link>
                </template>
              </el-table-column>
              <el-table-column label="服务项目" min-width="140">
                <template #default="{ row }">{{ serviceName(row.serviceId) }}</template>
              </el-table-column>
              <el-table-column label="客户" min-width="170">
                <template #default="{ row }">
                  <strong>{{ row.address?.contactName || '-' }}</strong>
                  <span class="admin-subtext">{{ row.address?.phone || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="技师" width="120">
                <template #default="{ row }">{{ technicianName(row.technicianId) }}</template>
              </el-table-column>
              <el-table-column label="金额" width="120">
                <template #default="{ row }">¥ {{ money(row.payableAmount) }}</template>
              </el-table-column>
              <el-table-column label="预约时间" width="140" prop="serviceTime" />
              <el-table-column label="状态" width="160">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.status)">{{ statusText[row.status] ?? row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="180">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
              </el-table-column>
            </el-table>
            <div class="admin-pagination">
              <el-pagination
                v-model:current-page="pagination.orders.page"
                v-model:page-size="pagination.orders.pageSize"
                background
                layout="total, sizes, prev, pager, next"
                :page-sizes="pageSizes"
                :total="filteredOrders.length"
              />
            </div>
          </el-card>
        </section>

        <section v-if="activeMenu === 'services'" class="admin-stack">
          <el-card shadow="never" class="admin-panel">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>商品管理</strong>
                  <span>新增、编辑、删除服务商品，并配置客户端展示封面图</span>
                </div>
                <el-button type="primary" :icon="Plus" @click="openServiceDrawer()">新增商品</el-button>
              </div>
            </template>
            <el-table :data="paginatedServices" stripe>
              <el-table-column label="封面" width="108">
                <template #default="{ row }">
                  <el-image v-if="row.imageUrl" class="admin-cover-thumb" :src="row.imageUrl" fit="cover">
                    <template #error>
                      <div class="admin-cover-thumb__empty">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="admin-cover-thumb admin-cover-thumb__empty">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="商品信息" min-width="220">
                <template #default="{ row }">
                  <strong>{{ row.name }}</strong>
                  <span class="admin-subtext">{{ row.slogan }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="durationMinutes" label="时长" width="90">
                <template #default="{ row }">{{ row.durationMinutes }} 分钟</template>
              </el-table-column>
              <el-table-column label="售价" width="120">
                <template #default="{ row }">¥ {{ money(row.price) }}</template>
              </el-table-column>
              <el-table-column label="划线价" width="120">
                <template #default="{ row }">¥ {{ money(row.originalPrice) }}</template>
              </el-table-column>
              <el-table-column prop="soldCount" label="销量" width="100" sortable />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link :icon="Edit" @click="openServiceDrawer(row)">编辑</el-button>
                  <el-button type="danger" link :icon="Delete" @click="deleteService(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="admin-pagination">
              <el-pagination
                v-model:current-page="pagination.services.page"
                v-model:page-size="pagination.services.pageSize"
                background
                layout="total, sizes, prev, pager, next"
                :page-sizes="pageSizes"
                :total="filteredServices.length"
              />
            </div>
          </el-card>
        </section>

        <section v-if="activeMenu === 'technicians'" class="admin-stack">
          <el-card shadow="never" class="admin-panel">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>技师管理</strong>
                  <span>新增、编辑、删除技师，并维护头像、服务范围和当前位置</span>
                </div>
                <el-button type="primary" :icon="Plus" @click="openTechnicianDrawer()">新增技师</el-button>
              </div>
            </template>
            <el-table :data="paginatedTechnicians" stripe height="560">
              <el-table-column label="技师" min-width="190">
                <template #default="{ row }">
                  <div class="admin-user-cell">
                    <el-avatar shape="square" :src="row.portraitUrl" />
                    <div>
                      <strong>{{ row.name }}</strong>
                      <span>{{ row.level }} · {{ row.newcomer ? '新人扶持' : '稳定接单' }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="servedOrders" label="服务单量" width="120" sortable />
              <el-table-column prop="likedCount" label="收藏" width="100" />
              <el-table-column prop="commentCount" label="评价" width="100" />
              <el-table-column label="可服务项目" min-width="220">
                <template #default="{ row }">
                  <el-space wrap>
                    <el-tag v-for="id in row.serviceIds" :key="id" effect="plain">{{ serviceName(id) }}</el-tag>
                  </el-space>
                </template>
              </el-table-column>
              <el-table-column label="最近可约" width="120" prop="nextAvailableTime" />
              <el-table-column label="当前位置" min-width="240">
                <template #default="{ row }">{{ row.location?.label ?? '未设置' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link :icon="Edit" @click="openTechnicianDrawer(row)">编辑</el-button>
                  <el-button type="danger" link :icon="Delete" @click="deleteTechnician(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="admin-pagination">
              <el-pagination
                v-model:current-page="pagination.technicians.page"
                v-model:page-size="pagination.technicians.pageSize"
                background
                layout="total, sizes, prev, pager, next"
                :page-sizes="pageSizes"
                :total="filteredTechnicians.length"
              />
            </div>
          </el-card>
        </section>

        <section v-if="activeMenu === 'users'" class="admin-stack">
          <el-card shadow="never" class="admin-panel">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>账号管理</strong>
                  <span>统一入口按账号角色进入管理端、技师端或客户端，技师角色需要绑定技师资料</span>
                </div>
                <el-button type="primary" :icon="Plus" @click="openAccountDrawer()">新增账号</el-button>
              </div>
            </template>
            <el-table :data="paginatedUsers" stripe height="560">
              <el-table-column label="账号" min-width="200">
                <template #default="{ row }">
                  <div class="admin-user-cell">
                    <el-avatar :icon="UserFilled" />
                    <div>
                      <strong>{{ row.name }}</strong>
                      <span>{{ row.username }} · {{ row.id }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" width="150" />
              <el-table-column label="角色" width="170">
                <template #default="{ row }">
                  <el-select v-model="row.role" @change="onUserRoleChange(row)">
                    <el-option label="管理员" value="ADMIN" />
                    <el-option label="客户" value="CLIENT" />
                    <el-option label="技师" value="TECHNICIAN" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="绑定技师" min-width="220">
                <template #default="{ row }">
                  <el-select
                    v-model="row.technicianId"
                    clearable
                    filterable
                    :disabled="row.role !== 'TECHNICIAN'"
                    placeholder="请选择技师资料"
                  >
                    <el-option
                      v-for="technician in technicians"
                      :key="technician.id"
                      :label="`${technician.name} · ${technician.id}`"
                      :value="technician.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="入口去向" width="140">
                <template #default="{ row }">
                  <el-tag :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="210" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link :icon="Edit" @click="saveUserRole(row)">保存角色</el-button>
                  <el-button type="warning" link @click="openPasswordDialog(row)">重置密码</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="admin-pagination">
              <el-pagination
                v-model:current-page="pagination.users.page"
                v-model:page-size="pagination.users.pageSize"
                background
                layout="total, sizes, prev, pager, next"
                :page-sizes="pageSizes"
                :total="filteredUsers.length"
              />
            </div>
          </el-card>
        </section>

        <section v-if="activeMenu === 'refunds'" class="admin-stack">
          <el-card shadow="never" class="admin-panel">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>售后退款</strong>
                  <span>处理退款申请并同步订单状态</span>
                </div>
                <el-tag type="warning">{{ refundOrders.length }} 单</el-tag>
              </div>
            </template>
            <el-table :data="paginatedRefundOrders" stripe empty-text="暂无退款售后">
              <el-table-column label="订单号" width="150">
                <template #default="{ row }">{{ shortId(row.id) }}</template>
              </el-table-column>
              <el-table-column label="客户" min-width="150">
                <template #default="{ row }">{{ row.address?.contactName || '-' }}</template>
              </el-table-column>
              <el-table-column label="服务" min-width="150">
                <template #default="{ row }">{{ serviceName(row.serviceId) }}</template>
              </el-table-column>
              <el-table-column label="金额" width="120">
                <template #default="{ row }">¥ {{ money(row.payableAmount) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="140">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.status)">{{ statusText[row.status] ?? row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === 'REFUND_REQUESTED'"
                    type="primary"
                    size="small"
                    @click="approveRefund(row)"
                  >
                    同意退款
                  </el-button>
                  <el-button v-else size="small" disabled>已处理</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="admin-pagination">
              <el-pagination
                v-model:current-page="pagination.refunds.page"
                v-model:page-size="pagination.refunds.pageSize"
                background
                layout="total, sizes, prev, pager, next"
                :page-sizes="pageSizes"
                :total="filteredRefundOrders.length"
              />
            </div>
          </el-card>
        </section>

        <section v-if="activeMenu === 'settings'" class="admin-stack">
          <el-card shadow="never" class="admin-panel">
            <template #header>
              <div class="admin-panel__header">
                <div>
                  <strong>基础设置</strong>
                  <span>维护客户端商户页展示的运营信息</span>
                </div>
                <el-button type="primary" :loading="settingsSaving" @click="saveSiteSettings">保存设置</el-button>
              </div>
            </template>
            <div class="admin-settings-grid">
              <el-form label-position="top" class="admin-settings-form">
                <el-form-item label="本地商家客服电话">
                  <el-input
                    v-model="siteSettingsForm.merchantServicePhone"
                    clearable
                    maxlength="30"
                    placeholder="请输入客服电话"
                  />
                </el-form-item>
                <el-alert
                  show-icon
                  :closable="false"
                  type="info"
                  title="保存后，客户端商户/技师列表页会立即使用新的客服电话展示与拨号。"
                />
              </el-form>
              <section class="admin-settings-preview">
                <span>客户端展示预览</span>
                <strong>本地商家客服电话：{{ siteSettingsForm.merchantServicePhone || '-' }}</strong>
                <small>位于商户列表页服务电话区域</small>
              </section>
            </div>
          </el-card>
        </section>

      </el-main>
    </el-container>

    <el-drawer v-model="orderDrawerVisible" size="420px" title="订单详情">
      <template v-if="selectedOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ selectedOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="服务">{{ serviceName(selectedOrder.serviceId) }}</el-descriptions-item>
          <el-descriptions-item label="技师">{{ technicianName(selectedOrder.technicianId) }}</el-descriptions-item>
          <el-descriptions-item label="客户">
            {{ selectedOrder.address?.contactName }} {{ selectedOrder.address?.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="地址">
            {{ selectedOrder.address?.region }} {{ selectedOrder.address?.detail }}
          </el-descriptions-item>
          <el-descriptions-item label="金额">¥ {{ money(selectedOrder.payableAmount) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(selectedOrder.status)">
              {{ statusText[selectedOrder.status] ?? selectedOrder.status }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <h3 class="admin-drawer-title">履约轨迹</h3>
        <el-timeline>
          <el-timeline-item
            v-for="item in selectedOrder.timeline"
            :key="`${item.status}-${item.happenedAt}`"
            :timestamp="formatTime(item.happenedAt)"
          >
            <strong>{{ statusText[item.status] ?? item.status }}</strong>
            <p>{{ item.message }}</p>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>

    <el-drawer v-model="serviceDrawerVisible" size="560px" :title="serviceDrawerTitle">
      <el-form label-position="top" class="admin-service-form">
        <section class="admin-cover-editor">
          <div class="admin-cover-preview">
            <el-image v-if="serviceForm.imageUrl" :src="serviceForm.imageUrl" fit="cover">
              <template #error>
                <div class="admin-cover-preview__empty">
                  <el-icon><Picture /></el-icon>
                  <span>封面读取失败</span>
                </div>
              </template>
            </el-image>
            <div v-else class="admin-cover-preview__empty">
              <el-icon><Picture /></el-icon>
              <span>待上传封面</span>
            </div>
          </div>
          <div class="admin-cover-editor__meta">
            <strong>商品封面</strong>
            <span>上传 JPG、PNG 或 WebP，建议使用 4:3 横图，最大 5MB。</span>
            <el-upload
              action="/api/merchant/uploads/service-cover"
              accept="image/jpeg,image/png,image/webp"
              :before-upload="beforeCoverUpload"
              :disabled="coverUploading"
              :http-request="uploadServiceCover"
              :show-file-list="false"
            >
              <el-button :icon="Upload" :loading="coverUploading">
                {{ serviceForm.imageUrl ? '替换照片' : '上传照片' }}
              </el-button>
            </el-upload>
          </div>
        </section>

        <el-form-item label="商品名称">
          <el-input v-model="serviceForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品卖点">
          <el-input v-model="serviceForm.slogan" placeholder="请输入一句话卖点" />
        </el-form-item>
        <div class="admin-service-form__grid">
          <el-form-item label="服务时长(分钟)">
            <el-input-number v-model="serviceForm.durationMinutes" :min="1" :step="10" />
          </el-form-item>
          <el-form-item label="售价">
            <el-input-number v-model="serviceForm.price" :min="0" :precision="2" :step="10" />
          </el-form-item>
          <el-form-item label="划线价">
            <el-input-number v-model="serviceForm.originalPrice" :min="0" :precision="2" :step="10" />
          </el-form-item>
          <el-form-item label="销量">
            <el-input-number v-model="serviceForm.soldCount" :min="0" :step="100" />
          </el-form-item>
        </div>

        <el-divider>商品详情</el-divider>
        <el-form-item v-for="field in detailFields" :key="field.key" :label="field.label">
          <el-input
            v-model="detailForm[field.key]"
            type="textarea"
            :rows="field.rows"
            :placeholder="field.placeholder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="serviceDrawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="serviceSaving" @click="saveService">保存商品</el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="technicianDrawerVisible" size="560px" :title="technicianDrawerTitle">
      <el-form label-position="top" class="admin-service-form">
        <section class="admin-cover-editor">
          <div class="admin-avatar-preview">
            <el-avatar v-if="technicianForm.portraitUrl" shape="square" :size="136" :src="technicianForm.portraitUrl" />
            <div v-else class="admin-avatar-preview__empty">
              <el-icon><UserFilled /></el-icon>
              <span>待上传头像</span>
            </div>
          </div>
          <div class="admin-cover-editor__meta">
            <strong>技师头像</strong>
            <span>上传 JPG、PNG 或 WebP，建议使用清晰正方形照片，最大 5MB。</span>
            <el-upload
              action="/api/merchant/uploads/technician-avatar"
              accept="image/jpeg,image/png,image/webp"
              :before-upload="beforeCoverUpload"
              :disabled="avatarUploading"
              :http-request="uploadTechnicianAvatar"
              :show-file-list="false"
            >
              <el-button :icon="Upload" :loading="avatarUploading">
                {{ technicianForm.portraitUrl ? '替换头像' : '上传头像' }}
              </el-button>
            </el-upload>
          </div>
        </section>

        <div class="admin-service-form__grid">
          <el-form-item label="姓名">
            <el-input v-model="technicianForm.name" placeholder="请输入技师姓名" />
          </el-form-item>
          <el-form-item label="等级">
            <el-input v-model="technicianForm.level" placeholder="例如 高级" />
          </el-form-item>
          <el-form-item label="服务单量">
            <el-input-number v-model="technicianForm.servedOrders" :min="0" :step="10" />
          </el-form-item>
          <el-form-item label="收藏数">
            <el-input-number v-model="technicianForm.likedCount" :min="0" :step="1" />
          </el-form-item>
          <el-form-item label="评价数">
            <el-input-number v-model="technicianForm.commentCount" :min="0" :step="1" />
          </el-form-item>
          <el-form-item label="最近可约">
            <el-input v-model="technicianForm.nextAvailableTime" placeholder="例如 23:00" />
          </el-form-item>
        </div>
        <el-form-item label="可服务项目">
          <el-select v-model="technicianForm.serviceIds" multiple placeholder="请选择服务商品">
            <el-option v-for="service in services" :key="service.id" :label="service.name" :value="service.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="technicianForm.newcomer">新人扶持</el-checkbox>
        </el-form-item>
        <el-divider>当前位置</el-divider>
        <el-form-item label="位置名称">
          <el-input v-model="technicianForm.location.label" placeholder="例如 武进区门店附近" />
        </el-form-item>
        <div class="admin-service-form__grid">
          <el-form-item label="纬度">
            <el-input-number v-model="technicianForm.location.latitude" :precision="6" :step="0.0001" />
          </el-form-item>
          <el-form-item label="经度">
            <el-input-number v-model="technicianForm.location.longitude" :precision="6" :step="0.0001" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="technicianDrawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="technicianSaving" @click="saveTechnician">保存技师</el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="accountDrawerVisible" size="520px" title="新增账号">
      <el-form label-position="top" class="admin-service-form">
        <div class="admin-service-form__grid">
          <el-form-item label="登录账号">
            <el-input v-model="accountForm.username" clearable placeholder="例如 admin2" />
          </el-form-item>
          <el-form-item label="登录密码">
            <el-input v-model="accountForm.password" show-password placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="accountForm.name" clearable placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="accountForm.phone" clearable placeholder="请输入手机号" />
          </el-form-item>
        </div>
        <el-form-item label="账号角色">
          <el-radio-group v-model="accountForm.role">
            <el-radio-button label="ADMIN">管理员</el-radio-button>
            <el-radio-button label="CLIENT">客户</el-radio-button>
            <el-radio-button label="TECHNICIAN">技师</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="accountForm.role === 'TECHNICIAN'" label="绑定技师资料">
          <el-select v-model="accountForm.technicianId" clearable filterable placeholder="请选择技师资料">
            <el-option
              v-for="technician in technicians"
              :key="technician.id"
              :label="`${technician.name} · ${technician.id}`"
              :value="technician.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDrawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="accountSaving" @click="createAccount">保存账号</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="420px">
      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input :model-value="passwordTarget?.username" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.password" show-password placeholder="至少 6 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="resetPassword">保存密码</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  DataAnalysis,
  Delete,
  Edit,
  Expand,
  Fold,
  Goods,
  List,
  Money,
  Picture,
  Plus,
  Refresh,
  Search,
  Service,
  Setting,
  Tickets,
  TrendCharts,
  Upload,
  UserFilled,
  WarningFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../services/api'
import { clearAdminUser, getAdminUser } from '../../services/auth'
import { orderTabs, statusText } from '../../services/status'

const router = useRouter()
const authUser = ref(getAdminUser())
const menuItems = computed(() => [
  { key: 'overview', label: '运营概览', icon: DataAnalysis },
  { key: 'orders', label: '订单管理', icon: List, badge: orders.value.length || '' },
  { key: 'services', label: '商品管理', icon: Goods, badge: services.value.length || '' },
  { key: 'technicians', label: '技师管理', icon: Service, badge: technicians.value.length || '' },
  { key: 'users', label: '账号管理', icon: UserFilled, badge: users.value.length || '' },
  { key: 'refunds', label: '售后退款', icon: Tickets, badge: refundOrders.value.length || '' },
  { key: 'settings', label: '基础设置', icon: Setting }
])

const isCollapsed = ref(false)
const activeMenu = ref('overview')
const loading = ref(false)
const keyword = ref('')
const orderTab = ref('ALL')
const pageSizes = [5, 10, 20, 50]
const pagination = reactive({
  orders: { page: 1, pageSize: 10 },
  services: { page: 1, pageSize: 10 },
  technicians: { page: 1, pageSize: 10 },
  users: { page: 1, pageSize: 10 },
  refunds: { page: 1, pageSize: 10 }
})
const dashboard = reactive({ todayOrders: 0, todayIncome: '0.00', pendingTasks: 0, satisfactionRate: '0%' })
const siteSettingsForm = reactive({ merchantServicePhone: '18069906336' })
const orders = ref([])
const services = ref([])
const technicians = ref([])
const users = ref([])
const editingServiceId = ref('')
const editingTechnicianId = ref('')
const orderDrawerVisible = ref(false)
const selectedOrder = ref(null)
const serviceDrawerVisible = ref(false)
const serviceSaving = ref(false)
const coverUploading = ref(false)
const technicianDrawerVisible = ref(false)
const technicianSaving = ref(false)
const avatarUploading = ref(false)
const settingsSaving = ref(false)
const accountDrawerVisible = ref(false)
const accountSaving = ref(false)
const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordTarget = ref(null)
const serviceForm = reactive({
  id: '',
  name: '',
  slogan: '',
  durationMinutes: 60,
  price: 0,
  originalPrice: 0,
  soldCount: 0,
  imageUrl: ''
})
const technicianForm = reactive(createEmptyTechnicianForm())
const accountForm = reactive(createEmptyAccountForm())
const passwordForm = reactive({ password: '' })
const detailForm = reactive({ highlights: '', process: '', materials: '', notices: '', contraindications: '', reasons: '' })
const detailFields = [
  { key: 'highlights', label: '项目亮点', placeholder: '每行一个亮点', rows: 3 },
  { key: 'process', label: '服务流程', placeholder: '每行一个步骤', rows: 4 },
  { key: 'materials', label: '服务物料', placeholder: '每行一个物料', rows: 2 },
  { key: 'notices', label: '注意事项', placeholder: '每行一条说明', rows: 3 },
  { key: 'contraindications', label: '服务禁忌', placeholder: '每行一条禁忌', rows: 4 },
  { key: 'reasons', label: '选择理由', placeholder: '每行一条理由', rows: 3 }
]

const activeTitle = computed(() => menuItems.value.find((item) => item.key === activeMenu.value)?.label)
const totalIncome = computed(() => orders.value.reduce((total, order) => total + Number(order.payableAmount ?? 0), 0))
const refundOrders = computed(() => orders.value.filter((order) => ['REFUND_REQUESTED', 'REFUNDED'].includes(order.status)))
const serviceDrawerTitle = computed(() => editingServiceId.value ? '编辑商品' : '新增商品')
const technicianDrawerTitle = computed(() => editingTechnicianId.value ? '编辑技师' : '新增技师')
const orderTabOptions = computed(() => orderTabs.map((item) => ({ label: item.label, value: item.key })))
const statusSummary = computed(() => Object.entries(orders.value.reduce((summary, order) => {
  const label = statusText[order.status] ?? order.status
  summary[label] = (summary[label] ?? 0) + 1
  return summary
}, {})).map(([label, count]) => ({ label, count })))
const filteredOrders = computed(() => {
  const tab = orderTabs.find((item) => item.key === orderTab.value)
  const text = keyword.value.trim().toLowerCase()
  return orders.value.filter((order) => {
    const matchTab = !tab?.statuses.length || tab.statuses.includes(order.status)
    const haystack = [
      order.id,
      order.address?.contactName,
      order.address?.phone,
      order.address?.region,
      serviceName(order.serviceId),
      technicianName(order.technicianId)
    ].join(' ').toLowerCase()
    return matchTab && (!text || haystack.includes(text))
  })
})
const filteredServices = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return services.value
  return services.value.filter((service) => [
    service.id,
    service.name,
    service.slogan
  ].join(' ').toLowerCase().includes(text))
})
const filteredTechnicians = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return technicians.value
  return technicians.value.filter((technician) => [
    technician.name,
    technician.level,
    technician.location?.label,
    ...technician.serviceIds.map(serviceName)
  ].join(' ').toLowerCase().includes(text))
})
const filteredUsers = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return users.value
  return users.value.filter((user) => [
    user.id,
    user.username,
    user.name,
    user.phone,
    roleLabel(user.role),
    technicianName(user.technicianId)
  ].join(' ').toLowerCase().includes(text))
})
const filteredRefundOrders = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return refundOrders.value
  return refundOrders.value.filter((order) => [
    order.id,
    order.address?.contactName,
    order.address?.phone,
    serviceName(order.serviceId),
    statusText[order.status] ?? order.status
  ].join(' ').toLowerCase().includes(text))
})
const paginatedOrders = computed(() => paginate(filteredOrders.value, pagination.orders))
const paginatedServices = computed(() => paginate(filteredServices.value, pagination.services))
const paginatedTechnicians = computed(() => paginate(filteredTechnicians.value, pagination.technicians))
const paginatedUsers = computed(() => paginate(filteredUsers.value, pagination.users))
const paginatedRefundOrders = computed(() => paginate(filteredRefundOrders.value, pagination.refunds))
const metrics = computed(() => [
  { label: '今日订单', value: dashboard.todayOrders, hint: `${orders.value.length} 单累计订单`, icon: List, tone: 'blue' },
  { label: '今日收入', value: `¥ ${money(dashboard.todayIncome)}`, hint: `累计 ¥ ${money(totalIncome.value)}`, icon: Money, tone: 'green' },
  { label: '待处理', value: dashboard.pendingTasks, hint: `${refundOrders.value.length} 单售后`, icon: WarningFilled, tone: 'orange' },
  { label: '满意度', value: dashboard.satisfactionRate, hint: `${technicians.value.length} 名技师在线`, icon: TrendCharts, tone: 'purple' }
])
const tasks = computed(() => [
  {
    title: refundOrders.value.length ? '处理退款申请' : '暂无退款待办',
    description: refundOrders.value.length ? `${refundOrders.value.length} 笔订单需要商户确认` : '售后队列为空',
    timestamp: '售后',
    type: refundOrders.value.length ? 'warning' : 'success'
  },
  {
    title: '维护服务详情',
    description: `${services.value.length} 个服务商品可编辑图文介绍`,
    timestamp: '商品',
    type: 'primary'
  },
  {
    title: '关注技师履约',
    description: `${technicians.value.length} 名技师覆盖 ${services.value.length} 类服务`,
    timestamp: '履约',
    type: 'info'
  }
])

onMounted(load)

watch([keyword, orderTab], () => {
  pagination.orders.page = 1
  pagination.services.page = 1
  pagination.technicians.page = 1
  pagination.users.page = 1
  pagination.refunds.page = 1
})

watch(() => filteredOrders.value.length, () => syncPage(pagination.orders, filteredOrders.value.length))
watch(() => filteredServices.value.length, () => syncPage(pagination.services, filteredServices.value.length))
watch(() => filteredTechnicians.value.length, () => syncPage(pagination.technicians, filteredTechnicians.value.length))
watch(() => filteredUsers.value.length, () => syncPage(pagination.users, filteredUsers.value.length))
watch(() => filteredRefundOrders.value.length, () => syncPage(pagination.refunds, filteredRefundOrders.value.length))

async function load() {
  loading.value = true
  try {
    const [nextDashboard, nextOrders, nextServices, nextTechnicians, nextUsers, nextSettings] = await Promise.all([
      api.merchantDashboard(),
      api.listOrders(),
      api.listServices(),
      api.listAllTechnicians(),
      api.listUsers(),
      api.merchantSiteSettings()
    ])
    Object.assign(dashboard, nextDashboard)
    Object.assign(siteSettingsForm, nextSettings)
    orders.value = nextOrders
    services.value = nextServices
    technicians.value = nextTechnicians
    users.value = nextUsers
  } finally {
    loading.value = false
  }
}

function onUserRoleChange(user) {
  if (user.role !== 'TECHNICIAN') {
    user.technicianId = null
  }
}

function openAccountDrawer() {
  Object.assign(accountForm, createEmptyAccountForm())
  accountDrawerVisible.value = true
}

async function createAccount() {
  if (!accountForm.username.trim()) {
    ElMessage.warning('请填写登录账号')
    return
  }
  if (!accountForm.password || accountForm.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  if (accountForm.role === 'TECHNICIAN' && !accountForm.technicianId) {
    ElMessage.warning('技师角色必须绑定技师资料')
    return
  }
  accountSaving.value = true
  try {
    const created = await api.createUser({
      username: accountForm.username.trim(),
      password: accountForm.password,
      name: accountForm.name.trim(),
      phone: accountForm.phone.trim(),
      role: accountForm.role,
      technicianId: accountForm.role === 'TECHNICIAN' ? accountForm.technicianId : null
    })
    users.value.unshift(created)
    accountDrawerVisible.value = false
    ElMessage.success('账号已创建')
  } finally {
    accountSaving.value = false
  }
}

async function saveUserRole(user) {
  if (user.role === 'TECHNICIAN' && !user.technicianId) {
    ElMessage.warning('技师角色必须绑定技师资料')
    return
  }
  const updated = await api.updateUserRole(user.id, {
    role: user.role,
    technicianId: user.role === 'TECHNICIAN' ? user.technicianId : null
  })
  const index = users.value.findIndex((item) => item.id === updated.id)
  if (index >= 0) users.value[index] = updated
  ElMessage.success('用户角色已保存')
}

function openPasswordDialog(user) {
  passwordTarget.value = user
  passwordForm.password = ''
  passwordDialogVisible.value = true
}

async function resetPassword() {
  if (!passwordTarget.value) return
  if (!passwordForm.password || passwordForm.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  passwordSaving.value = true
  try {
    await api.resetUserPassword(passwordTarget.value.id, { password: passwordForm.password })
    passwordDialogVisible.value = false
    ElMessage.success('密码已重置')
  } finally {
    passwordSaving.value = false
  }
}

function logout() {
  clearAdminUser()
  router.replace('/admin/login')
}

async function saveSiteSettings() {
  const phone = siteSettingsForm.merchantServicePhone.trim()
  if (!phone) {
    ElMessage.warning('请填写本地商家客服电话')
    return
  }
  settingsSaving.value = true
  try {
    const saved = await api.saveMerchantSiteSettings({ merchantServicePhone: phone })
    Object.assign(siteSettingsForm, saved)
    ElMessage.success('客服电话已保存')
  } finally {
    settingsSaving.value = false
  }
}

function openServiceDrawer(service) {
  editingServiceId.value = service?.id ?? ''
  Object.assign(serviceForm, service ? {
    id: service.id,
    name: service.name,
    slogan: service.slogan,
    durationMinutes: service.durationMinutes,
    price: Number(service.price),
    originalPrice: Number(service.originalPrice),
    soldCount: service.soldCount,
    imageUrl: service.imageUrl
  } : createEmptyServiceForm())

  if (service?.detail) {
    fillDetailForm(service.detail)
  } else {
    fillDetailForm(createEmptyDetail())
  }
  serviceDrawerVisible.value = true
}

function openTechnicianDrawer(technician) {
  editingTechnicianId.value = technician?.id ?? ''
  Object.assign(technicianForm, technician ? {
    id: technician.id,
    name: technician.name,
    level: technician.level,
    distanceKm: Number(technician.distanceKm ?? 0),
    servedOrders: technician.servedOrders,
    likedCount: technician.likedCount,
    commentCount: technician.commentCount,
    nextAvailableTime: technician.nextAvailableTime,
    newcomer: technician.newcomer,
    portraitUrl: technician.portraitUrl,
    location: {
      latitude: Number(technician.location?.latitude ?? 0),
      longitude: Number(technician.location?.longitude ?? 0),
      label: technician.location?.label ?? ''
    },
    serviceIds: [...(technician.serviceIds ?? [])]
  } : createEmptyTechnicianForm())
  technicianDrawerVisible.value = true
}

async function saveService() {
  if (!serviceForm.name) {
    ElMessage.warning('请填写商品名称')
    return
  }
  if (!serviceForm.imageUrl) {
    ElMessage.warning('请先上传商品封面图')
    return
  }
  serviceSaving.value = true
  try {
    const payload = {
      ...serviceForm,
      price: Number(serviceForm.price),
      originalPrice: Number(serviceForm.originalPrice),
      durationMinutes: Number(serviceForm.durationMinutes),
      soldCount: Number(serviceForm.soldCount),
      detail: detailPayload()
    }
    const updated = await api.saveService(payload)
    const index = services.value.findIndex((item) => item.id === updated.id)
    if (index >= 0) {
      services.value[index] = updated
    } else {
      services.value.unshift(updated)
    }
    serviceDrawerVisible.value = false
    ElMessage.success('商品已保存')
  } finally {
    serviceSaving.value = false
  }
}

async function deleteService(service) {
  await ElMessageBox.confirm(`确认删除「${service.name}」？删除后客户端将不再展示该商品。`, '删除商品', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  })
  await api.deleteService(service.id)
  services.value = services.value.filter((item) => item.id !== service.id)
  ElMessage.success('商品已删除')
}

async function saveTechnician() {
  if (!technicianForm.name) {
    ElMessage.warning('请填写技师姓名')
    return
  }
  if (!technicianForm.serviceIds.length) {
    ElMessage.warning('请至少选择一个可服务项目')
    return
  }
  technicianSaving.value = true
  try {
    const payload = {
      ...technicianForm,
      distanceKm: Number(technicianForm.distanceKm ?? 0),
      servedOrders: Number(technicianForm.servedOrders),
      likedCount: Number(technicianForm.likedCount),
      commentCount: Number(technicianForm.commentCount),
      location: {
        latitude: Number(technicianForm.location.latitude),
        longitude: Number(technicianForm.location.longitude),
        label: technicianForm.location.label || '未设置'
      },
      serviceIds: [...technicianForm.serviceIds]
    }
    const updated = await api.saveTechnician(payload)
    const index = technicians.value.findIndex((item) => item.id === updated.id)
    if (index >= 0) {
      technicians.value[index] = updated
    } else {
      technicians.value.unshift(updated)
    }
    technicianDrawerVisible.value = false
    ElMessage.success('技师已保存')
  } finally {
    technicianSaving.value = false
  }
}

async function deleteTechnician(technician) {
  await ElMessageBox.confirm(`确认删除「${technician.name}」？删除后客户端将不再展示该技师。`, '删除技师', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  })
  await api.deleteTechnician(technician.id)
  technicians.value = technicians.value.filter((item) => item.id !== technician.id)
  ElMessage.success('技师已删除')
}

function beforeCoverUpload(file) {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG、WebP 图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('封面图片不能超过 5MB')
    return false
  }
  return true
}

async function uploadServiceCover({ file, onSuccess, onError }) {
  coverUploading.value = true
  try {
    const result = await api.uploadServiceCover(file)
    serviceForm.imageUrl = result.url
    onSuccess(result)
    ElMessage.success('封面图已上传')
  } catch (error) {
    onError(error)
    ElMessage.error(error.message || '封面图上传失败')
  } finally {
    coverUploading.value = false
  }
}

async function uploadTechnicianAvatar({ file, onSuccess, onError }) {
  avatarUploading.value = true
  try {
    const result = await api.uploadTechnicianAvatar(file)
    technicianForm.portraitUrl = result.url
    onSuccess(result)
    ElMessage.success('头像已上传')
  } catch (error) {
    onError(error)
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

function detailPayload() {
  const payload = {}
  for (const field of detailFields) {
    payload[field.key] = detailForm[field.key].split('\n').map((item) => item.trim()).filter(Boolean)
  }
  return payload
}

function fillDetailForm(detail) {
  for (const field of detailFields) detailForm[field.key] = detail[field.key]?.join('\n') ?? ''
}

function createEmptyDetail() {
  return { highlights: [], process: [], materials: [], notices: [], contraindications: [], reasons: [] }
}

function createEmptyServiceForm() {
  return {
    id: '',
    name: '',
    slogan: '',
    durationMinutes: 60,
    price: 0,
    originalPrice: 0,
    soldCount: 0,
    imageUrl: ''
  }
}

function createEmptyTechnicianForm() {
  return {
    id: '',
    name: '',
    level: '高级',
    distanceKm: 0,
    servedOrders: 0,
    likedCount: 0,
    commentCount: 0,
    nextAvailableTime: '23:00',
    newcomer: false,
    portraitUrl: '',
    location: { latitude: 0, longitude: 0, label: '' },
    serviceIds: []
  }
}

function createEmptyAccountForm() {
  return {
    username: '',
    password: '',
    name: '',
    phone: '',
    role: 'CLIENT',
    technicianId: ''
  }
}

async function approveRefund(order) {
  await api.approveRefund(order.id)
  await load()
  ElMessage.success('已同意退款')
}

function openOrder(order) {
  selectedOrder.value = order
  orderDrawerVisible.value = true
}

function serviceName(serviceId) {
  return services.value.find((item) => item.id === serviceId)?.name ?? serviceId
}

function technicianName(technicianId) {
  return technicians.value.find((item) => item.id === technicianId)?.name ?? technicianId
}

function roleLabel(role) {
  if (role === 'ADMIN') return '管理端'
  if (role === 'TECHNICIAN') return '技师端'
  return '客户端'
}

function roleTagType(role) {
  if (role === 'ADMIN') return 'danger'
  if (role === 'TECHNICIAN') return 'success'
  return 'info'
}

function shortId(id) {
  return id?.length > 10 ? id.slice(0, 10) : id
}

function money(value) {
  return Number(value ?? 0).toFixed(2)
}

function paginate(items, state) {
  const start = (state.page - 1) * state.pageSize
  return items.slice(start, start + state.pageSize)
}

function syncPage(state, total) {
  const maxPage = Math.max(1, Math.ceil(total / state.pageSize))
  if (state.page > maxPage) state.page = maxPage
}

function statusPercent(count) {
  if (!orders.value.length) return 0
  return Math.round((count / orders.value.length) * 100)
}

function statusTagType(status) {
  if (['FINISHED', 'REFUNDED'].includes(status)) return 'success'
  if (['REFUND_REQUESTED', 'WAITING_PAYMENT'].includes(status)) return 'warning'
  if (['CANCELLED'].includes(status)) return 'info'
  return 'primary'
}

function formatTime(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}
</script>

<style scoped>
.merchant-admin {
  min-height: 100vh;
  color: #1f2937;
  background: #f5f7fb;
}

.merchant-admin__aside {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 20px 14px;
  background:
    linear-gradient(180deg, #159f86 0%, #0b7f6b 48%, #075f54 100%);
  transition: width 0.18s ease;
  overflow-x: hidden;
  overflow-y: auto;
}

.merchant-admin__aside.collapsed {
  padding-right: 10px;
  padding-left: 10px;
}

.merchant-admin__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 54px;
  padding: 6px 10px 14px;
  color: #fff;
}

.merchant-admin__brand strong,
.merchant-admin__brand span {
  display: block;
}

.merchant-admin__brand strong {
  font-size: 20px;
}

.merchant-admin__brand span {
  margin-top: 3px;
  color: rgba(236, 253, 245, 0.76);
  font-size: 12px;
}

.merchant-admin__logo {
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  color: #0b7f6b;
  background: #ffffff;
  border-radius: 8px;
  font-weight: 900;
}

.merchant-admin__collapse {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  min-height: 38px;
  margin: 0 0 10px;
  color: rgba(236, 253, 245, 0.82);
  background: rgba(255, 255, 255, 0.11);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 8px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}

.merchant-admin__collapse:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.merchant-admin__menu {
  flex: 1;
  border-right: 0;
  background: transparent;
}

.merchant-admin__menu:not(.el-menu--collapse) {
  width: 100%;
}

.merchant-admin__menu :deep(.el-menu-item) {
  height: 46px;
  margin: 4px 0;
  color: rgba(236, 253, 245, 0.84);
  border-radius: 8px;
}

.merchant-admin__menu :deep(.el-menu-item.is-active) {
  color: #0b7f6b;
  background: #ffffff;
  box-shadow: 0 12px 30px rgba(6, 95, 70, 0.22);
}

.merchant-admin__menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.14);
}

.merchant-admin__menu.el-menu--collapse :deep(.el-menu-item) {
  justify-content: center;
  padding: 0 !important;
}

.merchant-admin__menu em {
  margin-left: auto;
  padding: 1px 7px;
  color: #0b7f6b;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 999px;
  font-style: normal;
  font-size: 12px;
}

.merchant-admin__aside-card {
  display: grid;
  gap: 8px;
  margin-top: 14px;
  padding: 14px;
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 8px;
}

.merchant-admin__aside-card span,
.merchant-admin__aside-card small {
  color: rgba(236, 253, 245, 0.78);
}

.merchant-admin__aside-card strong {
  color: #fff;
}

.merchant-admin__operator {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding: 12px;
  color: #fff;
  background: rgba(255, 255, 255, 0.13);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
}

.merchant-admin__operator strong,
.merchant-admin__operator span {
  display: block;
}

.merchant-admin__operator span {
  margin-top: 3px;
  color: rgba(236, 253, 245, 0.74);
  font-size: 12px;
}

.merchant-admin__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: auto;
  min-height: 96px;
  padding: 18px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.merchant-admin__header h1 {
  margin: 8px 0 4px;
  font-size: 24px;
}

.merchant-admin__header p {
  margin: 0;
  color: #64748b;
}

.merchant-admin__header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.merchant-admin__header-actions .el-input {
  width: 280px;
}

.merchant-admin__main {
  padding: 20px 24px 32px;
}

.admin-metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.admin-metric-card,
.admin-panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.admin-metric-card {
  position: relative;
  min-height: 142px;
  padding: 18px;
}

.admin-metric-card span,
.admin-metric-card small {
  display: block;
  color: #64748b;
}

.admin-metric-card strong {
  display: block;
  margin-top: 12px;
  color: #0f172a;
  font-size: 28px;
}

.admin-metric-card small {
  margin-top: 8px;
}

.admin-metric-card__icon {
  position: absolute;
  top: 18px;
  right: 18px;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  font-size: 20px;
}

.admin-metric-card__icon.blue {
  color: #2563eb;
  background: #eff6ff;
}

.admin-metric-card__icon.green {
  color: #059669;
  background: #ecfdf5;
}

.admin-metric-card__icon.orange {
  color: #d97706;
  background: #fffbeb;
}

.admin-metric-card__icon.purple {
  color: #7c3aed;
  background: #f5f3ff;
}

.admin-content-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 16px;
}

.admin-panel--wide {
  min-width: 0;
}

.admin-stack {
  display: grid;
  gap: 16px;
}

.admin-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.admin-panel__header strong,
.admin-panel__header span {
  display: block;
}

.admin-panel__header strong {
  color: #0f172a;
  font-size: 16px;
}

.admin-panel__header span {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.admin-status-board {
  display: grid;
  gap: 12px;
}

.admin-status-board article {
  display: grid;
  grid-template-columns: 160px 56px 1fr;
  gap: 14px;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.admin-status-board span {
  color: #475569;
}

.admin-status-board strong {
  color: #0f172a;
  font-size: 20px;
}

.admin-user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-user-cell strong,
.admin-user-cell span,
.admin-subtext {
  display: block;
}

.admin-user-cell span,
.admin-subtext {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.admin-drawer-title {
  margin: 22px 0 16px;
  color: #0f172a;
}

.admin-pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

.admin-cover-thumb {
  display: block;
  width: 76px;
  height: 58px;
  border-radius: 8px;
  overflow: hidden;
  background: #edf7f4;
}

.admin-cover-thumb__empty {
  display: grid;
  place-items: center;
  color: #159f86;
  border: 1px dashed rgba(21, 159, 134, 0.45);
}

.admin-cover-editor {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 16px;
  margin-bottom: 16px;
  padding: 14px;
  background: #f7fbf9;
  border: 1px solid #dbeee9;
  border-radius: 8px;
}

.admin-cover-preview {
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #edf7f4;
  border-radius: 8px;
}

.admin-cover-preview :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.admin-cover-preview__empty {
  display: grid;
  place-items: center;
  gap: 8px;
  width: 100%;
  height: 100%;
  min-height: 150px;
  color: #159f86;
  border: 1px dashed rgba(21, 159, 134, 0.45);
  border-radius: 8px;
}

.admin-cover-preview__empty .el-icon {
  font-size: 28px;
}

.admin-avatar-preview {
  display: grid;
  place-items: center;
  width: 100%;
  min-height: 166px;
  background: #edf7f4;
  border-radius: 8px;
}

.admin-avatar-preview__empty {
  display: grid;
  place-items: center;
  gap: 8px;
  width: 136px;
  height: 136px;
  color: #159f86;
  border: 1px dashed rgba(21, 159, 134, 0.45);
  border-radius: 8px;
}

.admin-avatar-preview__empty .el-icon {
  font-size: 28px;
}

.admin-cover-editor__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 10px;
}

.admin-cover-editor__meta strong,
.admin-cover-editor__meta span {
  display: block;
}

.admin-cover-editor__meta strong {
  color: #0f172a;
  font-size: 16px;
}

.admin-cover-editor__meta span {
  color: #64748b;
  line-height: 1.6;
}

.admin-settings-grid {
  display: grid;
  grid-template-columns: minmax(0, 520px) minmax(280px, 1fr);
  gap: 18px;
  align-items: start;
}

.admin-settings-form {
  padding: 18px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.admin-settings-preview {
  display: grid;
  gap: 10px;
  padding: 20px;
  color: #0f172a;
  background: #f7fbf9;
  border: 1px solid #dbeee9;
  border-radius: 8px;
}

.admin-settings-preview span,
.admin-settings-preview small {
  color: #64748b;
}

.admin-settings-preview strong {
  color: #0b7f6b;
  font-size: 22px;
}

.admin-service-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.admin-service-form__grid :deep(.el-input-number) {
  width: 100%;
}

.admin-service-form :deep(.el-select) {
  width: 100%;
}

@media (max-width: 1100px) {
  .admin-metric-grid,
  .admin-content-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 860px) {
  .merchant-admin {
    display: block;
  }

  .merchant-admin__aside {
    position: static;
    width: 100% !important;
    height: auto;
  }

  .merchant-admin__aside.collapsed {
    width: 100% !important;
  }

  .merchant-admin__header,
  .merchant-admin__header-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .merchant-admin__header-actions .el-input,
  .admin-metric-grid,
  .admin-content-grid,
  .admin-cover-editor,
  .admin-settings-grid,
  .admin-service-form__grid {
    width: 100%;
    grid-template-columns: 1fr;
  }
}
</style>
