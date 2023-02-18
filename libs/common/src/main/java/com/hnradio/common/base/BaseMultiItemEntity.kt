package com.hnradio.common.base

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.base
 * @ClassName: BaseMultiItemEntity
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/7/13 10:53 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/7/13 10:53 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
open class BaseMultiItemEntity<T>(val data: T, override val itemType: Int) : MultiItemEntity{
}