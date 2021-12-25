import 'package:desktop/desktop.dart';

class NonEmptyTextFormField extends StatefulWidget {
  const NonEmptyTextFormField({
    Key? key,
    required this.onSave,
    required this.buttonTitle,
    this.emptyTooltip,
  }) : super(key: key);

  final ValueChanged<String> onSave;
  final String buttonTitle;
  final String? emptyTooltip;

  @override
  _NonEmptyTextFieldState createState() => _NonEmptyTextFieldState();
}

class _NonEmptyTextFieldState extends State<NonEmptyTextFormField> {
  final TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    _controller.addListener(_controllerListener);
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Flexible(child: TextField(controller: _controller)),
        Flexible(
          child: Button.text(
            widget.buttonTitle,
            tooltip: _controller.text.isEmpty ? widget.emptyTooltip : null,
            onPressed: _controller.text.isNotEmpty ? _onSave : null,
          ),
        ),
      ],
    );
  }

  void _controllerListener() => setState(() {});

  void _onSave() {
    widget.onSave(_controller.text);
  }

  @override
  void dispose() {
    _controller.removeListener(_controllerListener);
    super.dispose();
  }
}
