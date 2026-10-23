import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';
import 'data/draft_store.dart';

void main() => runApp(const CafeJuninApp());
class CafeJuninApp extends StatelessWidget {
  const CafeJuninApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(title:'Café Junín', theme:ThemeData(colorScheme:ColorScheme.fromSeed(seedColor:const Color(0xff32624a)), scaffoldBackgroundColor:const Color(0xfff7f3ec), useMaterial3:true),home:const CapturePage());
}
class CapturePage extends StatefulWidget {
  const CapturePage({super.key});
  @override State<CapturePage> createState()=>_CapturePageState();
}
class _CapturePageState extends State<CapturePage> {
  final _store=DraftStore(),_form=GlobalKey<FormState>();
  final _parcel=TextEditingController(),_weight=TextEditingController();
  String _variety='BOURBON',_collection='SELECTIVA';DateTime _date=DateTime.now();
  List<Map<String,dynamic>> _drafts=[];bool _busy=false;String? _error;
  @override void initState(){super.initState();_refresh();}
  @override void dispose(){_parcel.dispose();_weight.dispose();super.dispose();}
  Future<void> _refresh() async {try{final items=await _store.all();if(mounted)setState(()=>_drafts=items);}catch(_){if(mounted)setState(()=>_error='No se pudo abrir el almacenamiento local.');}}
  Future<void> _save() async {
    if(!_form.currentState!.validate())return;
    setState((){_busy=true;_error=null;});
    try {
      final id=const Uuid().v4();
      await _store.save({'id':id,'parcelaId':_parcel.text.trim(),'fechaCosecha':'${_date.year}-${_date.month.toString().padLeft(2,'0')}-${_date.day.toString().padLeft(2,'0')}','variedad':_variety,'tipoRecoleccion':_collection,'pesoKg':double.parse(_weight.text)});
      _weight.clear();await _refresh();
      if(mounted)ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content:Text('Borrador guardado en este dispositivo.')));
    } catch(_){if(mounted)setState(()=>_error='No se pudo guardar. Tus borradores anteriores se conservan.');}
    finally{if(mounted)setState(()=>_busy=false);}
  }
  @override Widget build(BuildContext context)=>Scaffold(appBar:AppBar(title:const Text('Café Junín · Campo')),body:ListView(padding:const EdgeInsets.all(20),children:[
    const Card(child:Padding(padding:EdgeInsets.all(16),child:Text('FASE MÓVIL PRELIMINAR · Captura de borradores sin conexión. No inicia sesión ni sincroniza con el servidor. No usar con información sensible; SQLite aún no está cifrado.'))),
    const SizedBox(height:20),Text('Registrar cosecha local',style:Theme.of(context).textTheme.headlineSmall),
    if(_error!=null)Text(_error!,style:const TextStyle(color:Colors.red)),
    Form(key:_form,child:Column(crossAxisAlignment:CrossAxisAlignment.stretch,children:[
      TextFormField(controller:_parcel,decoration:const InputDecoration(labelText:'Identificador de parcela (UUID)'),validator:(v)=>RegExp(r'^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$').hasMatch(v?.trim()??'')?null:'Ingresa un UUID válido.'),
      TextFormField(controller:_weight,decoration:const InputDecoration(labelText:'Peso neto (kg)'),keyboardType:const TextInputType.numberWithOptions(decimal:true),validator:(v){final n=double.tryParse(v??'');return n==null||!n.isFinite||n<=0||n>99999999.999||!RegExp(r'^\d+(\.\d{1,3})?$').hasMatch(v!)?'Peso válido con hasta tres decimales.':null;}),
      DropdownButtonFormField<String>(value:_variety,decoration:const InputDecoration(labelText:'Variedad'),items:['BOURBON','TYPICA','GEISHA','CATIMOR'].map((v)=>DropdownMenuItem(value:v,child:Text(v))).toList(),onChanged:(v)=>setState(()=>_variety=v!)),
      DropdownButtonFormField<String>(value:_collection,decoration:const InputDecoration(labelText:'Recolección'),items:['SELECTIVA','GENERAL'].map((v)=>DropdownMenuItem(value:v,child:Text(v))).toList(),onChanged:(v)=>setState(()=>_collection=v!)),
      TextButton.icon(icon:const Icon(Icons.calendar_month),label:Text('Cosecha: ${_date.day}/${_date.month}/${_date.year}'),onPressed:()async{final d=await showDatePicker(context:context,initialDate:_date,firstDate:DateTime(2020),lastDate:DateTime.now());if(d!=null&&mounted)setState(()=>_date=d);}),
      FilledButton.icon(onPressed:_busy?null:_save,icon:const Icon(Icons.save_outlined),label:Text(_busy?'Guardando…':'Guardar borrador offline')),
    ])),const SizedBox(height:28),Text('Borradores locales (${_drafts.length})',style:Theme.of(context).textTheme.titleLarge),
    if(_drafts.isEmpty)const Padding(padding:EdgeInsets.all(16),child:Text('Todavía no hay borradores en este equipo.')),
    ..._drafts.map((d)=>Card(child:ListTile(leading:const Icon(Icons.cloud_off),title:Text('${d['variedad']} · ${d['pesoKg']} kg'),subtitle:Text('${d['fechaCosecha']}\n${d['id']}\nPendiente de autenticación y sincronización'),isThreeLine:true))),
  ]));
}
