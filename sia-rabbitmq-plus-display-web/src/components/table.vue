<template>
	<div>
		<table cellspacing="0" cellpadding="2" frame="box" class="customer_gift_list text_c" id="customerGiftList">
			<colgroup>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
				<col width="100"></col>
			</colgroup>
			<tr class="font14 tr_golden">
				<th>任务类型</th>
				<th>端口</th>
				<th>ip地址</th>
				<th>方法名</th>
				<th>类名</th>
				<th>bean名称</th>
				<th>任务参数</th>
				<th>任务ID</th>
				<th>应用名</th>
				<th>状态</th>
				<th>操作</th>
				<th>详情</th>
			</tr>
			<tr v-for="item in tableList" class="customerList_tr">
				<td>
					<div v-text="item.taskType" :v-title="item.taskType" class="text_td"></div>
				</td>
				<td>
					<div v-text="item.port" class="text_td"></div>
				</td>
				<td>
					<div v-text="item.ip" class="text_td"></div>
				</td>
				<td>
					<div v-text="item.methodName" class="text_td"></div>
				</td>
				<td>
					<div v-text="item.className" class="text_td"></div>
				</td>
				<td>
					<div v-text="item.beanName" class="text_td"></div>
				</td>
				<td>
					<div v-text="item.taskParameter" :v-title="item.taskParameter" class="text_td"></div>
				</td>
				<td>
					<div v-text="item.taskId" class="text_td"></div>
				</td>
				<td>
					<div v-text="item.applicationName" class="text_td"></div>
				</td>
				<td>
					<div class="text_td">
						<span v-if="item.status == 'true'">运行</span>
						<span v-else >暂停</span>
					</div>
				</td>
				<td>
					<img v-if="item.status == 'true'"  src="../scss/img/enable_cur.png" @click="end(item)" title="暂停" alt="暂停" />
					<img v-else="item.status == 'false'"  src="../scss/img/enable.png" @click="start(item)"  title="运行" alt="运行" />
				</td>
				<td>
					<img src="../scss/img/eye.png" title="查看详情" alt="运行" @click="showdetail(item)" />
				</td>
			</tr>
		</table>
		<div class="del_pop">
			<div class="delpop_disk"></div>
			<div class="meun_context">
				<div class="meun_context_top">
					<span class="top_left">查看详情</span>
					<img class="top_right" @click="hideDetail" src="../scss/img/icon_10.png" />
				</div>
                <div class="meun_context_content">
                	<div class="content_line">
                		<p class="line_name">&nbsp;&nbsp;&nbsp;任务类型  </p><p class="line_content" v-text="detail.taskType"></p>
                	</div>
                	<div class="content_line">
                		<p class="line_name">&nbsp;&nbsp;&nbsp;端口  </p><p class="line_content" v-text="detail.port"></p>
                	</div>
                	<div class="content_line">
                		<p class="line_name">&nbsp;&nbsp;&nbsp;IP地址  </p><p class="line_content" v-text="detail.ip"></p>
                	</div>
                	<div class="content_line">
                		<p class="line_name">&nbsp;&nbsp;&nbsp;方法名  </p><p class="line_content" v-text="detail.methodName"></p>
                	</div>
                	<div class="content_line">
                		<p class="line_name" style="height: 80px;line-height: 80px;">&nbsp;&nbsp;&nbsp;类名  </p><p class="line_content" v-text="detail.className"></p>
                	</div>
                	<div class="content_line">
                		<p class="line_name">&nbsp;&nbsp;&nbsp;bean名称  </p><p class="line_content" v-text="detail.beanName"></p>
                	</div>
                	<div class="content_line">
                		<p class="line_name" style="height: 160px;line-height: 160px;">&nbsp;&nbsp;&nbsp;任务参数  </p><p class="line_content" v-text="detail.taskParameter"></p>
                	</div>
                	<div class="content_line">
                		<p class="line_name">&nbsp;&nbsp;&nbsp;任务ID  </p><p class="line_content" v-text="detail.taskId"></p>
                	</div>
                	<div class="content_line">
                		<p class="line_name">&nbsp;&nbsp;&nbsp;应用名  </p><p class="line_content" v-text="detail.applicationName"></p>
                	</div>
                	<div class="content_line border_boot">
                		<p class="line_name">&nbsp;&nbsp;&nbsp;状态  </p>
                		<p class="line_content">
                			<span v-if="detail.status == 'true'">运行</span>
						    <span v-else >暂停</span>
                		</p>
                	</div>
                </div>
			</div>
		</div>
	</div>
</template>
<style lang="sass-loader" type="text/scss" scoped>
     /* 弹窗 */ 
     .del_pop { 
     	position: fixed; 
     	z-index: 9999; 
     	top: 0; 
     	left: 0; 
     	width: 100%; 
     	height: 100%; 
     	display: none;
     	} 
     	.delpop_disk {
     		background-color: #000; 
     		filter: alpha(opacity=30); 
     		opacity: .3; 
     		width: 100%;
     	    height: 100%; 
     	    } 
     	    .del_pop .meun_context { 
     	    	position: absolute; 
     	    	z-index: 99999; 
     	    	left: 50%; 
     	    	top: 50%; 
     	    	margin-left: -289px; 
     	    	margin-top: -182px; 
     	    	width: 576px; 
     	    	height: 369px; 
     	    	background: #FFFFFF;
                border-radius: 6px;
     	    	.meun_context_content{
     	    		width: 576px;
     	    		height: 316px;
     	    		padding-top: 20px;
     	    		padding-bottom: 20px;
     	    		overflow-y:scroll;
     	    		.border_boot{
     	    			border-bottom:1px solid #CCCCCC;
     	    		}	
     	    		
     	    		.content_line{
     	    			width: 519px;
     	    			border-left:1px solid #CCCCCC;
     	    			border-right:1px solid #CCCCCC;
     	    			border-top:1px solid #CCCCCC;
     	    			height: auto;
     	    			margin-left: 20px;
     	    			overflow: hidden;
     	    			
     	    			.line_name{
     	    				float: left;
     	    				width: 80px;
     	    				color: #fff;
     	    				background: #e3c191;
     	    				line-height: 40px;
     	    			}
     	    			.line_content{
     	    				color: #666666;
     	    				float: left;
     	    				width: 437px;
                            word-break: break-word;
     	    				line-height: 40px;
     	    				padding-left: 10px;
     	    				height: auto;
     	    				
     	    				
     	    			}
     	    		}
     	    		
     	    	}
     	    	
     	        .meun_context_top { 
     	    		font-size: 13px; 
     	    		width: 576px; 
     	    		height: 50px; 
     	    		color: #fff;
     	    		border-top-right-radius: 6px;
     	    		border-top-left-radius: 6px;
                    background: #e3c191;
     	    		line-height: 50px; 
     	    		
     	    		.top_left { 
     	    			float: left; 
     	    		    font-size: 18px; 
     	    		    color: #fff; 
     	    		    height: 30px; 
     	    		    display: inline-block; 
     	    		    line-height: 30px; 
     	    		    margin-top: 10px; 
     	    		    margin-left: 10px; 
     	    		    padding-left: 10px; 
     	    		    } 
 	    		    .top_right { 
 	    		    	float: right; 
 	    		    	margin-top: 15px; 
 	    		    	margin-right: 20px; 
 	    		    	height: 15px; 
 	    		    	width: 15px; 
 	    		    	cursor: pointer; 
 	    		    	} 
     	    		} 
     	    	} 
      /* 弹窗结束 */ 
    	.customerList_tr { 
    		text-align: center; 
    		border: 1px #E1E1E8 solid; 
    		color: #666666; 
    		height: 40px; 
    		background: #fff; } 
    	.tr_golden { 
    		color: #fff; 
    		background: #e3c191; 
    		height: 48px; } 
    	.customer_gift_list { 
    		width: 100%; 
    		tr { 
    			td { 
    				word-break: break-word; 
    				img{
    					margin-left: 10px;cursor: pointer;
    				}
    				
    				.text_td { 
    					width: 115px; 
    					height: 40px; 
    					line-height: 40px; 
    					overflow: hidden;
						text-overflow:ellipsis;
						white-space: nowrap;
    					
    					  } 
    					} 
    					&:nth-child(odd) { 
    						background: #F4F4F4; 
    						} 
    					} 
    				}
</style>

<script> 
	
	export default {
		data() {
			return {
				tableList: [],
				apiUrl: 'http://10.143.128.216:17021/getTasks',
				detail:{},
				heardUrl:'http://10.143.128.216:17021/startTask',
				heardUrl2:'http://10.143.128.216:17021/stopTask'
				
			}
		},
		components: {},
		mounted() {
			this.getList();
		},
		updated() {

		},
		watch: {
			$route(to, from) {

			}
		},
		methods: {
			getList() {
				let _this = this;

				_this.$http.get(baseUrl+"/getTasks")
					.then((response) => {
						for(let i = 0; i < response.body.length; i++) {
							_this.tableList.push(JSON.parse(response.body[i]));
						}
					})
					.catch((response) => {
						console.log(response)
					})

			},

			start(item) {
				let _this = this;
				
				
				let startURL = baseUrl + 'startTask?ip='+item.ip+'&port='+item.port+'&beanName='+item.beanName
				console.log(startURL)
				
				_this.$http.get(startURL)
					.then((response) => {
						console.log(response)
                       item.status = response.bodyText;
					})
					.catch((response) => {
						console.log(response)
					})
			},

			end(item) {
				let _this = this;
				let startURL = baseUrl + 'stopTask?ip='+item.ip+'&port='+item.port+'&beanName='+item.beanName
				
				console.log(startURL)
				_this.$http.get(startURL)
					.then((response) => {
						console.log(response)
						item.status = response.bodyText 
                        
					})
					.catch((response) => {
						console.log(response)
					})
			},
			showdetail(item)
			{
				this.detail = item;
				$('.del_pop').show()
			},
			hideDetail(){
				$('.del_pop').hide()
			}
			
		}
	}
</script>