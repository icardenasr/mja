package com.xabber.android.service.ws_client.service_ws;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.xabber.android.service.constantes.Urls;
import com.xabber.android.service.dto.UsuarioDTO;
import com.xabber.android.service.ws_client.CallWSAsyn;
import com.xabber.android.ui.own.LoginAct;

public class UsuarioWS {

	public static final String KEY_USUARIO = "usuario";

	public static final int LOGIN = 1;
	public static final int UPDATE = 2;
	public static final int UPDATE_PASSWORD = 3;

	private String aux;
	private HashMap<Integer, Object> paramsWS;

	public UsuarioWS(Integer typeAction, Context ctx, List<String> params,
			HashMap<Integer, Object> paramsWS, String aux) {

		this.aux = aux;
		this.paramsWS = paramsWS;

		if (typeAction != null) {
			switch (typeAction) {
			case LOGIN:
				if (paramsWS != null) {
					// paramsWS.put(CallWSAsyn.PARAMS_URL, Urls.WS_USER_LOGIN);
				}

				break;

			case UPDATE:
				if (paramsWS != null) {
					// paramsWS.put(CallWSAsyn.PARAMS_URL, Urls.WS_USER_UPDATE);
				}

				break;

			case UPDATE_PASSWORD:
				if (paramsWS != null) {
					// paramsWS.put(CallWSAsyn.PARAMS_URL,
					// Urls.WS_USER_UPDATE_PASS);
				}

				break;

			default:
				break;
			}
		}

		new MakeCall(typeAction, ctx, params, paramsWS).execute();
	}

	public class MakeCall extends CallWSAsyn {

		private Integer typeAction;

		public MakeCall(Integer typeAction, Context ctx, List<String> params,
				HashMap<Integer, Object> paramsWS) {
			super(ctx, params, paramsWS);

			this.typeAction = typeAction;

		}

		@Override
		protected void onResultCorrect() {
			super.onResultCorrect();
			// resutlObjectList
			if (typeAction != null) {
				switch (typeAction) {
				case LOGIN:

					// if (ctx instanceof Activity) {
					//
					// UsuarioDTO usuResult = null;
					// if (resutlObjectList != null
					// && !resutlObjectList.isEmpty()) {
					// usuResult = (UsuarioDTO) resutlObjectList.get(0);
					// }
					//
					// if (usuResult != null && aux != null) {
					// usuResult.setPassword(aux);
					// }
					//
					//

					if (((Activity) ctx) instanceof LoginAct) {

						UsuarioDTO usuResult = (UsuarioDTO) paramsWS
								.get(PARAMS_DATA_POST);

						if (usuResult != null) {

							// UtilPreferences.setPreferences(ctx,
							// KEY_USUARIO, usuResult);
							((LoginAct) ctx).loginChat((UsuarioDTO) usuResult);

						}
					}
					// }

					break;

				case UPDATE:

					// if (ctx instanceof Activity) {
					// UsuarioDTO usuResult = null;
					// if (resutlObjectList != null
					// && !resutlObjectList.isEmpty()) {
					// usuResult = (UsuarioDTO) resutlObjectList.get(0);
					// }
					// if (usuResult != null) {
					//
					// if (((Activity) ctx) instanceof SettingEditarAct) {
					//
					// UtilPreferences.setPreferences(ctx,
					// KEY_USUARIO, usuResult);
					//
					// Toast.makeText(ctx,
					// ctx.getString(R.string.guard_corre),
					// Toast.LENGTH_SHORT).show();
					//
					// ((Activity) ctx).finish();
					//
					// }
					// }
					//
					// }

					break;

				case UPDATE_PASSWORD:

					// if (ctx instanceof Activity) {
					// UsuarioDTO usuResult = null;
					// if (resutlObjectList != null
					// && !resutlObjectList.isEmpty()) {
					// usuResult = (UsuarioDTO) resutlObjectList.get(0);
					// }
					// if (usuResult != null) {
					//
					// if (((Activity) ctx) instanceof PasswordEditarAct) {
					//
					// UsuarioDTO usu = (UsuarioDTO) UtilPreferences
					// .getPreferences(ctx, KEY_USUARIO,
					// UsuarioDTO.class);
					//
					// if (usu != null && aux != null) {
					// usu.setPassword(aux);
					// }
					//
					// UtilPreferences.setPreferences(ctx,
					// KEY_USUARIO, usu);
					//
					// Toast.makeText(ctx,
					// ctx.getString(R.string.pass_act_corr),
					// Toast.LENGTH_SHORT).show();
					//
					// ((Activity) ctx).finish();
					//
					// }
					// }
					//
					// }

					break;

				default:
					break;
				}
			}

			// Toast.makeText(ctx, "Bieeeen", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onResultDefaultError() {
			super.onResultDefaultError();
			if (typeAction != null) {
				switch (typeAction) {
				case LOGIN:

					// UsuarioDTO usuarioDTO = new UsuarioDTO();
					// usuarioDTO.setNick("Jose");
					// usuarioDTO.setPassword("1383734cc13db894a26e184e8e66da87");
					//
					// if (ctx instanceof Activity) {
					//
					// if (((Activity) ctx) instanceof LoginAct) {
					// ((LoginAct) ctx).loginChat((UsuarioDTO) usuarioDTO);
					// }
					// }
					// Toast.makeText(ctx, getString(R.string.guard_corre),
					// Toast.LENGTH_SHORT).show();

					if (((Activity) ctx) instanceof LoginAct) {

						UsuarioDTO usuResult = (UsuarioDTO) paramsWS
								.get(PARAMS_DATA_POST);

						if (usuResult != null) {

							// UtilPreferences.setPreferences(ctx,
							// KEY_USUARIO, usuResult);
							((LoginAct) ctx).loginChat((UsuarioDTO) usuResult);

						}
					}

					break;
				case UPDATE:

					break;

				case UPDATE_PASSWORD:

					break;
				default:
					break;
				}
			}

			// Toast.makeText(ctx, "Maaaal", Toast.LENGTH_SHORT).show();
		}
	}

}
