package opcUaServer;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext.GetAttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext.SetAttributeContext;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeLoggingFilter implements AttributeFilter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Predicate<AttributeId> attributePredicate;
	private Field field;
	private Object skill;

	public AttributeLoggingFilter() {
		this(attributeId -> true);
	}

	public AttributeLoggingFilter(Predicate<AttributeId> attributePredicate) {
		this.attributePredicate = attributePredicate;
	}

	public AttributeLoggingFilter(Predicate<AttributeId> attributePredicate, Field field, Object skill) {
		this.attributePredicate = attributePredicate;
		this.field = field;
		this.skill = skill;
	}

	@Override
	public Object getAttribute(GetAttributeContext ctx, AttributeId attributeId) {
		Object value = ctx.getAttribute(attributeId);

		// only log external reads
		if (attributePredicate.test(attributeId) && ctx.getSession().isPresent()) {
			DataValue dataValue = (DataValue) value;
			try {
				setField(field, field.getType(), dataValue, skill);

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return value;
	}

	@Override
	public void setAttribute(SetAttributeContext ctx, AttributeId attributeId, Object value) {
		// only log external writes
		if (attributePredicate.test(attributeId) && ctx.getSession().isPresent()) {
			DataValue dataValue = (DataValue) value;
			try {
				setField(field, field.getType(), dataValue, skill);
				logger.info("set nodeId={} attributeId={} value={}", ctx.getNode().getNodeId(), attributeId, value);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ctx.setAttribute(attributeId, value);
	}

	public void setField(Field field, Class<?> type, DataValue dataValue, Object skill)
			throws IllegalArgumentException, IllegalAccessException {
		if (type == boolean.class) {
			field.set(skill, (boolean) dataValue.getValue().getValue());
		} else if (type == byte.class) {
			field.set(skill, (byte) dataValue.getValue().getValue());
		} else if (type == short.class) {
			field.set(skill, (short) dataValue.getValue().getValue());
		} else if (type == int.class) {
			field.set(skill, (int) dataValue.getValue().getValue());
		} else if (type == long.class) {
			field.set(skill, (long) dataValue.getValue().getValue());
		} else if (type == float.class) {
			field.set(skill, (float) dataValue.getValue().getValue());
		} else if (type == double.class) {
			field.set(skill, (double) dataValue.getValue().getValue());
		}
	}
}
