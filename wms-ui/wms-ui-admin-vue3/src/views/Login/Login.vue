<template>
  <div :class="prefixCls" class="relative h-[100%] overflow-hidden">
    <div class="login-ambient login-ambient--one"></div>
    <div class="login-ambient login-ambient--two"></div>
    <div class="login-grid"></div>
    <div class="login-scan-line"></div>

    <div class="relative z-1 mx-auto h-full flex p-32px lt-lg:p-20px lt-md:p-12px">
      <div class="login-left relative flex-1 overflow-hidden rounded-24px p-36px lt-xl:hidden">
        <div class="login-brand flex items-center">
          <img alt="" class="login-brand__logo" src="@/assets/imgs/logo.png" />
          <span class="login-brand__title">{{ systemTitle }}</span>
        </div>

        <div class="login-left__content">
          <div class="login-left__tag">SMART WAREHOUSE MANAGEMENT</div>
          <h1 class="login-left__headline">智能仓储管理平台</h1>
          <p class="login-left__desc">
            贯通入库、上架、拣选、复核、出库与库存协同，实时掌控库位状态，提升物料流转效率。
          </p>
          <div class="login-flow-card">
            <div class="login-flow-card__track"></div>
            <div class="login-flow-card__point login-flow-card__point--one"></div>
            <div class="login-flow-card__point login-flow-card__point--two"></div>
            <div class="login-flow-card__point login-flow-card__point--three"></div>
            <div class="login-flow-card__label login-flow-card__label--in">INBOUND</div>
            <div class="login-flow-card__label login-flow-card__label--stock">STOCK</div>
            <div class="login-flow-card__label login-flow-card__label--out">OUTBOUND</div>
          </div>
          <div class="login-metrics">
            <div>
              <strong>库位协同</strong>
              <span>Storage Location</span>
            </div>
            <div>
              <strong>库存可视</strong>
              <span>Inventory Insight</span>
            </div>
            <div>
              <strong>任务流转</strong>
              <span>Task Dispatch</span>
            </div>
          </div>
        </div>
      </div>

      <div class="login-right relative flex-1 overflow-x-hidden overflow-y-auto rounded-24px p-30px lt-md:p-16px">
        <div class="flex items-center justify-between at-2xl:justify-end at-xl:justify-end">
          <div class="login-brand login-brand--mobile flex items-center at-2xl:hidden at-xl:hidden">
            <img alt="" class="login-brand__logo" src="@/assets/imgs/logo.png" />
            <span class="login-brand__title">{{ systemTitle }}</span>
          </div>
          <div class="flex items-center justify-end space-x-10px h-48px">
            <ThemeSwitch />
            <LocaleDropdown />
          </div>
        </div>

        <Transition appear enter-active-class="animate__animated animate__fadeInRight">
          <div class="login-form-wrap">
            <div class="login-panel-header">
              <img alt="" class="login-panel-header__logo" src="@/assets/imgs/logo.png" />
              <div class="login-panel-header__title">{{ systemTitle }}</div>
              <div class="login-panel-header__desc">物料流转 · 库存协同 · 作业可视</div>
            </div>
            <LoginForm class="login-panel" />
            <ForgetPasswordForm class="login-panel" />
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { underlineToHump } from '@/utils'

import { useDesign } from '@/hooks/web/useDesign'
import { useAppStore } from '@/store/modules/app'
import { ThemeSwitch } from '@/layout/components/ThemeSwitch'
import { LocaleDropdown } from '@/layout/components/LocaleDropdown'

import { LoginForm, ForgetPasswordForm } from './components'

defineOptions({ name: 'Login' })

const appStore = useAppStore()
const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('login')
const systemTitle = computed(() => underlineToHump(appStore.getTitle).replace(/^捷圣\s*/, ''))
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-login;

.#{$prefix-cls} {
  min-height: 100%;
  overflow: auto;
  background:
    radial-gradient(circle at 18% 18%, rgb(20 184 166 / 22%), transparent 32%),
    radial-gradient(circle at 88% 12%, rgb(37 99 235 / 24%), transparent 30%),
    linear-gradient(135deg, #061525 0%, #09263c 48%, #07111f 100%);
  color: #e5f7ff;
}

.login-grid {
  position: absolute;
  inset: 0;
  opacity: 0.18;
  background-image:
    linear-gradient(rgb(125 211 252 / 18%) 1px, transparent 1px),
    linear-gradient(90deg, rgb(125 211 252 / 18%) 1px, transparent 1px);
  background-size: 46px 46px;
}

.login-scan-line {
  position: absolute;
  top: -20%;
  left: -30%;
  width: 48%;
  height: 140%;
  background: linear-gradient(90deg, transparent, rgb(34 211 238 / 18%), transparent);
  transform: rotate(18deg);
  animation: scan-line 5.5s linear infinite;
}

.login-ambient {
  position: absolute;
  border-radius: 999px;
  filter: blur(8px);
  opacity: 0.7;

  &--one {
    right: 16%;
    bottom: 18%;
    width: 260px;
    height: 260px;
    background: rgb(14 165 233 / 16%);
  }

  &--two {
    top: 10%;
    left: 8%;
    width: 180px;
    height: 180px;
    background: rgb(45 212 191 / 15%);
  }
}

.login-left {
  border: 1px solid rgb(125 211 252 / 18%);
  background: linear-gradient(145deg, rgb(8 47 73 / 72%), rgb(8 13 30 / 42%));
  box-shadow: inset 0 0 40px rgb(34 211 238 / 8%);
}

.login-right {
  margin-left: 24px;
  border: 1px solid rgb(125 211 252 / 12%);
  background: rgb(241 245 249 / 94%);
  color: var(--el-text-color-primary);
  backdrop-filter: blur(18px);

  .dark & {
    background: rgb(8 13 30 / 80%);
  }
}

.login-brand {
  gap: 12px;

  &__logo {
    width: 128px;
    height: 38px;
    object-fit: contain;
  }

  &__title {
    padding-left: 12px;
    border-left: 1px solid rgb(226 232 240 / 36%);
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 1px;
    line-height: 1;
    color: #f8fdff;
  }

  &--mobile {
    .login-brand__title {
      color: var(--el-text-color-primary);
      border-left-color: var(--el-border-color);
    }
  }
}

.login-left__content {
  position: relative;
  z-index: 1;
  height: calc(100% - 70px);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-left__tag {
  width: fit-content;
  margin-bottom: 18px;
  padding: 6px 12px;
  border: 1px solid rgb(34 211 238 / 28%);
  border-radius: 999px;
  color: #67e8f9;
  font-size: 12px;
  letter-spacing: 1.8px;
}

.login-left__headline {
  margin: 0;
  font-size: 42px;
  font-weight: 800;
  letter-spacing: 2px;
  line-height: 1.2;
}

.login-left__desc {
  max-width: 560px;
  margin: 20px 0 34px;
  color: rgb(224 242 254 / 78%);
  font-size: 16px;
  line-height: 1.9;
}

.login-flow-card {
  position: relative;
  width: min(560px, 86%);
  height: 170px;
  overflow: hidden;
  border: 1px solid rgb(56 189 248 / 24%);
  border-radius: 20px;
  background:
    linear-gradient(135deg, rgb(14 165 233 / 18%), transparent),
    rgb(2 6 23 / 28%);

  &::before {
    position: absolute;
    inset: 18px;
    border: 1px dashed rgb(125 211 252 / 22%);
    border-radius: 14px;
    content: '';
  }

  &__track {
    position: absolute;
    top: 82px;
    left: 52px;
    width: calc(100% - 104px);
    height: 4px;
    border-radius: 999px;
    background: linear-gradient(90deg, #22d3ee, #38bdf8, #2dd4bf);
    box-shadow: 0 0 18px rgb(34 211 238 / 70%);

    &::after {
      position: absolute;
      top: -4px;
      left: 0;
      width: 60px;
      height: 12px;
      border-radius: 999px;
      background: #ecfeff;
      box-shadow: 0 0 18px #67e8f9;
      animation: flow-light 3.8s ease-in-out infinite;
      content: '';
    }
  }

  &__point {
    position: absolute;
    top: 68px;
    width: 32px;
    height: 32px;
    border: 2px solid #67e8f9;
    border-radius: 8px;
    background: rgb(8 47 73 / 88%);
    box-shadow: 0 0 16px rgb(34 211 238 / 44%);

    &--one {
      left: 50px;
    }

    &--two {
      left: calc(50% - 16px);
    }

    &--three {
      right: 50px;
    }
  }

  &__label {
    position: absolute;
    top: 112px;
    color: rgb(224 242 254 / 72%);
    font-size: 12px;
    letter-spacing: 1px;

    &--in {
      left: 40px;
    }

    &--stock {
      left: calc(50% - 28px);
    }

    &--out {
      right: 28px;
    }
  }
}

.login-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  width: min(560px, 86%);
  margin-top: 22px;

  div {
    padding: 14px;
    border: 1px solid rgb(125 211 252 / 18%);
    border-radius: 14px;
    background: rgb(15 23 42 / 32%);
  }

  strong,
  span {
    display: block;
  }

  strong {
    color: #f8fafc;
    font-size: 15px;
  }

  span {
    margin-top: 6px;
    color: rgb(186 230 253 / 58%);
    font-size: 12px;
  }
}

.login-form-wrap {
  width: min(440px, 100%);
  min-height: calc(100% - 60px);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-panel-header {
  margin-bottom: 22px;
  text-align: center;

  &__logo {
    width: 150px;
    height: 44px;
    object-fit: contain;
  }

  &__title {
    margin-top: 12px;
    font-size: 28px;
    font-weight: 800;
    letter-spacing: 1px;
  }

  &__desc {
    margin-top: 8px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }
}

.login-panel {
  position: relative;
  padding: 30px 28px 22px;
  overflow: hidden;
  border: 1px solid rgb(14 165 233 / 16%);
  border-radius: 22px;
  background: rgb(255 255 255 / 92%);
  box-shadow: 0 24px 60px rgb(15 23 42 / 16%);

  &::before {
    position: absolute;
    top: 0;
    left: 18px;
    right: 18px;
    height: 1px;
    background: linear-gradient(90deg, transparent, #22d3ee, transparent);
    animation: panel-glow 4s ease-in-out infinite;
    content: '';
  }

  :deep(.el-button--primary) {
    border: 0;
    background: linear-gradient(90deg, #0284c7, #06b6d4, #14b8a6);
    box-shadow: 0 12px 24px rgb(6 182 212 / 24%);
  }

  :deep(.el-input__wrapper) {
    border-radius: 12px;
  }

  .dark & {
    background: rgb(15 23 42 / 86%);
    box-shadow: 0 24px 60px rgb(0 0 0 / 28%);
  }
}

@keyframes scan-line {
  0% {
    transform: translateX(-20%) rotate(18deg);
  }

  100% {
    transform: translateX(280%) rotate(18deg);
  }
}

@keyframes flow-light {
  0% {
    transform: translateX(0);
    opacity: 0;
  }

  12% {
    opacity: 1;
  }

  100% {
    transform: translateX(420px);
    opacity: 0;
  }
}

@keyframes panel-glow {
  0%,
  100% {
    opacity: 0.35;
  }

  50% {
    opacity: 1;
  }
}
</style>

<style lang="scss">
.dark .login-form {
  .el-divider__text {
    background-color: var(--login-bg-color);
  }

  .el-card {
    background-color: var(--login-bg-color);
  }
}
</style>
